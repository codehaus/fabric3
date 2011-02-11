package org.fabric3.runtime.embedded.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

/**
 * @author Michal Capo
 */
public class EmbeddedBootstrapHelper {

    public static ClassLoader createClassLoader(ClassLoader parent, File... directories) {
        List<File> jars = new ArrayList<File>();
        for (File directory : directories) {
            jars.addAll(Arrays.asList(directory.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if (file.isHidden()) {
                        return false;
                    }
                    if (file.isDirectory()) {
                        return true;
                    }
                    try {
                        JarFile jar = new JarFile(file);
                        return jar.getManifest() != null;
                    } catch (IOException e) {
                        return false;
                    }
                }
            })));
        }

        URL[] urls = new URL[jars.size()];
        for (int i = 0; i < jars.size(); i++) {
            try {
                urls[i] = jars.get(i).toURI().toURL();
            } catch (MalformedURLException e) {
                // toURI should have escaped the URL
                throw new AssertionError();
            }
        }
        return new URLClassLoader(urls, parent);
    }


}
