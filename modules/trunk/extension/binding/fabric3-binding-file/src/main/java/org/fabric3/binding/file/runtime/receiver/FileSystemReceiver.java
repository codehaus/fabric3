/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.binding.file.runtime.receiver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.fabric3.binding.file.common.Strategy;
import org.fabric3.host.util.FileHelper;
import org.fabric3.host.util.IOHelper;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;

/**
 */
public class FileSystemReceiver implements Runnable {
    private File path;
    private Pattern filePattern;
    private File lockDirectory;
    private Strategy strategy;
    private File errorDirectory;
    private File archiveDirectory;

    private long delay = 2000;  // FIXME

    private Interceptor interceptor;
    private ScheduledExecutorService executorService;
    private ReceiverMonitor monitor;

    private Map<String, FileEntry> cache = new ConcurrentHashMap<String, FileEntry>();
    private ScheduledFuture<?> future;

    public FileSystemReceiver(ReceiverConfiguration configuration) {
        this.path = configuration.getLocation();
        this.strategy = configuration.getStrategy();
        this.errorDirectory = configuration.getErrorLocation();
        this.archiveDirectory = configuration.getArchiveLocation();
        this.filePattern = configuration.getFilePattern();
        this.interceptor = configuration.getInterceptor();
        this.monitor = configuration.getMonitor();
        this.lockDirectory = configuration.getLockDirectory();
    }

    public void start() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        future = executorService.scheduleWithFixedDelay(this, delay, delay, TimeUnit.MILLISECONDS);
        if (!lockDirectory.exists()) {
            lockDirectory.mkdirs();
        }
    }

    public void stop() {
        if (future != null) {
            future.cancel(true);
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public synchronized void run() {
        List<File> files = new ArrayList<File>();
        if (!path.isDirectory()) {
            // there is no drop directory, return without processing
            return;
        }
        File[] pathFiles = path.listFiles();
        for (File file : pathFiles) {
            // add files
            if (filePattern.matcher(file.getName()).matches()) {
                files.add(file);
            }
        }
        if (pathFiles != null) {
            Collections.addAll(files, pathFiles);
        }
        if (files.isEmpty()) {
            // there are no files to process
            return;
        }
        try {
            processFiles(files);
        } catch (RuntimeException e) {
            monitor.error(e);
        } catch (Error e) {
            monitor.error(e);
            throw e;
        }
    }

    private synchronized void processFiles(List<File> files) {
        for (File file : files) {
            String name = file.getName();
            if (ignore(file, name)) continue;
            FileEntry cached = cache.get(name);
            if (cached == null) {
                // the file is new, cache it and wait for next run in case it is in the process of being updated
                cached = new FileEntry(file);
                cache.put(name, cached);
            } else {
                if (!cached.isChanged()) {
                    // file has finished being updated, process it
                    processFile(file);
                }
            }
        }
    }

    private boolean ignore(File file, String name) {
        if (name.startsWith(".") || file.isDirectory()) {
            // skip hidden files
            return true;
        }
        return false;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private void processFile(File file) {
        String name = file.getName();
        // remove file from the cache as it will either be processed by this runtime or skipped
        cache.remove(name);
        // attempt to lock the file
        InputStream stream = null;
        FileChannel lockChannel = null;
        FileLock fileLock = null;
        File lockFile = new File(lockDirectory, file.getName() + ".f3");
        try {
            try {
                // Always attempt to lock since a lock file could have been created by this or another VM before it crashed. In this case,
                // the lock file is orphaned and another VM must continue processing.
                lockChannel = new RandomAccessFile(lockFile, "rw").getChannel();
                fileLock = lockChannel.tryLock();
                if (fileLock == null) {
                    return;
                }
                stream = createInputStream(file);
            } catch (OverlappingFileLockException e) {
                // already being processed by this VM, ignore
                return;
            } catch (IOException e) {
                // error acquiring the lock or creating the input stream, skip the file
                monitor.error(e);
                return;
            }
            Message response = dispatch(stream);
            if (response.isFault()) {
                // TODO error handling
            }

        } finally {
            IOHelper.closeQuietly(stream);
            if (Strategy.ARCHIVE == strategy) {
                archiveFile(file);
            }
            if (file.exists()) {
                file.delete();
            }
            releaseLock(fileLock);
            IOHelper.closeQuietly(lockChannel);
            if (lockFile.exists()) {
                lockFile.delete();
            }
        }
    }

    private Message dispatch(InputStream stream) {
        Message message = new MessageImpl();
        WorkContext workContext = new WorkContext();
        message.setWorkContext(workContext);
        message.setBody(new Object[]{stream});
        return interceptor.invoke(message);
    }

    private void archiveFile(File file) {
        try {
            FileHelper.copyFile(file, new File(archiveDirectory, file.getName()));
        } catch (IOException e) {
            monitor.error(e);
        }
    }

    private void releaseLock(FileLock fileLock) {
        if (fileLock != null) {
            try {
                fileLock.release();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private InputStream createInputStream(File file) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(file));
    }


}
