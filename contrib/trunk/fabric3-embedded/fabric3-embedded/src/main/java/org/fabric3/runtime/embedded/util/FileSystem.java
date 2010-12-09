package org.fabric3.runtime.embedded.util;

import org.fabric3.runtime.embedded.EmbeddedServerImpl;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3SetupException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author Michal Capo
 */
public class FileSystem {

    /**
     * Temporary property identifier.
     */
    private static String tmpDir = "java.io.tmpdir";

    /**
     * Random generator.
     */
    private static Random random = new Random();

    public static String temporaryFolder() {
        return System.getProperty(tmpDir);
    }

    public static String generateFolderName() {
        return "fabric3_" + Math.abs(random.nextInt());
    }

    public static boolean isAbsolute(final String path) {
        return new File(path).isAbsolute();
    }

    public static List<File> folders(File basePath, String... folders) {
        List<File> result = new ArrayList<File>();

        for (String folder : folders) {
            result.add(folder(basePath.getAbsolutePath() + File.separator + folder));
        }

        return result;
    }

    public static List<File> folders(String... folders) {
        List<File> result = new ArrayList<File>();

        for (String folder : folders) {
            result.add(folder(folder));
        }

        return result;
    }

    public static File folder(File path, String folderString) {
        return new File(path.getAbsolutePath() + File.separator + folderString);
    }

    public static File folder(String folderString) {
        return new File(folderString);
    }

    public static List<File> createFolders(List<File> folders) {
        List<File> result = new ArrayList<File>();

        for (File folder : folders) {
            result.add(createFolder(folder));
        }

        return result;
    }

    public static List<File> createFolders(File... folders) {
        return createFolders(Arrays.asList(folders));
    }

    public static File createFolder(File folder) {
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new EmbeddedFabric3SetupException("Cannot create folder: '" + folder + "'. Check file permission.");
            }
        }

        return folder;
    }

    public static void copy(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static void copy(URL sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        URLConnection url = sourceFile.openConnection();
        InputStream source = url.getInputStream();
        FileOutputStream target = new FileOutputStream(destFile);
        try {
            url.connect();

            final int BUF_SIZE = 1 << 8;
            byte[] buffer = new byte[BUF_SIZE];
            int bytesRead = -1;
            while ((bytesRead = source.read(buffer)) > -1) {
                target.write(buffer, 0, bytesRead);
            }
        }
        finally {
            target.close();

            if (source != null) {
                source.close();
            }
        }
    }


    public static List<File> filesIn(String folder) {
        return Arrays.asList(folder(folder).listFiles());
    }

    public static List<File> filesIn(File folder) {
        File[] files = folder.listFiles();
        if (null == files) {
            return new ArrayList<File>();
        }

        return Arrays.asList(files);
    }

    public static File file(String folderString) {
        return new File(folderString);
    }

    public static File file(File path, String fileName) {
        return new File(path.getAbsolutePath() + File.separator + fileName);
    }

    public static URL fileAtClassPath(String fileClassPath) throws URISyntaxException, MalformedURLException {
        URL resource = EmbeddedServerImpl.class.getResource(fileClassPath);
        if (null == resource) {
            throw new EmbeddedFabric3SetupException(String.format("File '%1$s' couldn't be found on classpath.", fileClassPath));
        }
        return resource.toURI().toURL();
    }

    public static void checkExistence(File... files) {
        for (File f : files) {
            checkExistence(f);
        }
    }

    public static void checkExistenceAndContent(File... files) {
        for (File f : files) {
            checkExistenceAndContent(f);
        }
    }

    public static void checkExistence(File file) {
        if (!exists(file)) {
            throw new EmbeddedFabric3SetupException("File/folder : '" + file + "' doesn't exists.");
        }
    }

    public static void checkExistenceAndContent(File file) {
        if (!exists(file)) {
            throw new EmbeddedFabric3SetupException("File/folder : '" + file + "' doesn't exists.");
        }

        if (file.isDirectory() && 0 == file.listFiles().length) {
            throw new EmbeddedFabric3SetupException("Folder : '" + file + "' doesn't contain any files.");
        }
    }

    public static boolean exists(final String filePath) {
        return new File(filePath).exists();
    }

    public static boolean exists(File file) {
        return null != file && file.exists();
    }

    public static void delete(List<File> files) {
        for (File file : files) {
            delete(file);
        }
    }

    public static void delete(File path, String... folders) {
        for (String f : folders) {
            delete(new File(path, f));
        }
    }

    public static boolean delete(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (null != files) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        delete(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return folder.delete();
    }

}
