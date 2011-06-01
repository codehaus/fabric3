package org.fabric3.assembly.utils;

import org.fabric3.assembly.exception.AssemblyException;

import java.io.*;
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
public class FileUtils {

    /**
     * Random generator.
     */
    private static Random random = new Random();

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
                throw new AssemblyException("Cannot create folder: '" + folder + "'. Check file permission.");
            }
        }

        return folder;
    }

    public static void copy(File sourceFile, File destinationFile) throws IOException {
        if (!destinationFile.exists()) {
            if (!destinationFile.createNewFile()) {
                throw new AssemblyException("Cannot create file/folder: {0}", destinationFile.getAbsoluteFile());
            }
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destinationFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static void copy(URL sourceFile, File destinationFile) throws IOException {
        if (!destinationFile.exists()) {
            if (!destinationFile.createNewFile()) {
                throw new AssemblyException("Cannot create file/folder: {0}", destinationFile.getAbsoluteFile());
            }
        }

        URLConnection url = sourceFile.openConnection();
        InputStream source = url.getInputStream();
        FileOutputStream target = new FileOutputStream(destinationFile);
        try {
            url.connect();

            final int BUF_SIZE = 1 << 8;
            byte[] buffer = new byte[BUF_SIZE];
            int bytesRead;
            while ((bytesRead = source.read(buffer)) > -1) {
                target.write(buffer, 0, bytesRead);
            }
        } finally {
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

    public static File file(File path, String... fileName) {
        String temp = "";
        for (String s : fileName) {
            temp += File.separator + s;
        }
        return new File(path.getAbsolutePath() + temp);
    }

    public static URL fileAtClassPath(String fileClassPath) throws URISyntaxException, MalformedURLException {
        URL resource = FileUtils.class.getResource(fileClassPath);
        if (null == resource) {
            throw new AssemblyException(String.format("File '%1$s' couldn't be found on classpath.", fileClassPath));
        }
        return resource.toURI().toURL();
    }

    public static void checkExistence(File... files) {
        for (File f : files) {
            checkExistence(f);
        }
    }

    public static void checkExistenceAndContent(List<File> files) {
        checkExistence(files.toArray(new File[files.size()]));
    }

    public static void checkExistenceAndContent(File... files) {
        for (File f : files) {
            checkExistenceAndContent(f);
        }
    }

    public static void checkExistence(File file) {
        if (!exists(file)) {
            throw new AssemblyException("File/folder : '" + file + "' doesn't exists.");
        }
    }

    public static void checkExistenceAndContent(File file) {
        if (!exists(file)) {
            throw new AssemblyException("File/folder : '" + file + "' doesn't exists.");
        }

        if (file.isDirectory() && 0 == file.listFiles().length) {
            throw new AssemblyException("Folder : '" + file + "' doesn't contain any files.");
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

    public static void delete(File path, String... files) {
        for (String f : files) {
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
                        if (!file.delete()) {
                            LoggerUtils.logWarn("Cannot delete file/folder: {0}", file.getAbsoluteFile());
                        }
                    }
                }
            }
        }
        return folder.delete();
    }

}
