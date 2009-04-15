/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.services.artifact;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.artifact.ArtifactCache;
import org.fabric3.spi.services.artifact.CacheException;
import org.fabric3.util.io.FileHelper;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class FSArtifactCache implements ArtifactCache {
    private File tempDir;
    private Map<URI, Entry> entries;

    public FSArtifactCache(@Reference HostInfo info) {
        tempDir = new File(info.getTempDir(), "cache");
        entries = new HashMap<URI, Entry>();
    }

    @Init
    public void init() throws IOException {
        if (tempDir.exists()) {
            FileHelper.deleteDirectory(tempDir);
        }
        tempDir.mkdirs();
    }

    public synchronized URL cache(URI uri, InputStream stream) throws CacheException {
        if (entries.containsKey(uri)) {
            throw new CacheRuntimeException("Entry for URI already exists: " + uri);
        }
        try {
            File file = new File(tempDir, uri.toString());
            FileHelper.write(stream, file);
            URL url = file.toURI().toURL();
            Entry entry = new Entry(url, file);
            entries.put(uri, entry);
            file.deleteOnExit();
            return url;
        } catch (IOException e) {
            throw new CacheException(e);
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                throw new CacheException(e);
            }
        }
    }

    public synchronized URL get(URI uri) {
        Entry entry = entries.get(uri);
        if (entry == null) {
            return null;
        }
        return entry.getEntryURL();
    }

    public synchronized void increment(URI uri) {
        Entry entry = entries.get(uri);
        if (entry == null) {
            throw new CacheRuntimeException("Entry for URI not found:" + uri);
        }
        entry.getCounter().getAndIncrement();
    }

    public synchronized boolean release(URI uri) {
        Entry entry = entries.get(uri);
        if (entry == null) {
            return false;
        }
        int i = entry.getCounter().decrementAndGet();
        if (i == 0) {
            entry.getFile().delete();
            entries.remove(uri);
            return true;
        }
        return false;
    }

    public synchronized int getCount(URI uri) {
        Entry entry = entries.get(uri);
        if (entry == null) {
            return 0;
        }
        return entry.getCounter().get();
    }

    private class Entry {
        private AtomicInteger counter;
        private URL entryURL;
        private File file;

        private Entry(URL entryURL, File file) {
            this.entryURL = entryURL;
            this.file = file;
            counter = new AtomicInteger(1);
        }

        public AtomicInteger getCounter() {
            return counter;
        }

        public URL getEntryURL() {
            return entryURL;
        }

        public File getFile() {
            return file;
        }
    }
}
