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
package org.fabric3.fabric.services.contribution.archive;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.util.io.IOHelper;
import org.fabric3.spi.services.contribution.archive.ClasspathProcessor;
import org.fabric3.spi.services.contribution.archive.ClasspathProcessorRegistry;

/**
 * Creates a classpath based on the contents of a jar. Specifically, adds the jar and any zip/jar archives found in META-INF/lib to the classpath
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JarClasspathProcessor implements ClasspathProcessor {
    private final ClasspathProcessorRegistry registry;

    public JarClasspathProcessor(@Reference ClasspathProcessorRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    @Destroy
    public void destroy() {
        registry.unregister(this);
    }


    public boolean canProcess(URL url) {
        String name = url.getFile().toLowerCase();
        return name.endsWith(".jar") || name.endsWith(".zip");
    }

    public List<URL> process(URL url) throws IOException {
        List<URL> classpath = new ArrayList<URL>();
        // add the the jar itself to the classpath
        classpath.add(url);

        // add libraries from the jar
        addLibraries(classpath, url);
        return classpath;
    }

    private void addLibraries(List<URL> classpath, URL jar) throws IOException {
        
        File dir = new File(System.getProperty("java.io.tmpdir"), ".f3");
        
        //String parentArchive = jar.toString().substring(jar.toString().lastIndexOf("/"));
        //dir = new File(dir, parentArchive);
        
        dir.mkdirs();
        InputStream is = jar.openStream();
        try {
            JarInputStream jarStream = new JarInputStream(is);
            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String path = entry.getName();
                if (!path.startsWith("META-INF/lib/")) {
                    continue;
                }
                // File jarFile = File.createTempFile("fabric3", ".jar", dir);
                String fileName = path.substring(path.lastIndexOf('/'));
                File jarFile = new File(dir, fileName);
                if (!jarFile.exists()) {
                    jarFile.createNewFile();
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(jarFile));
                    try {
                        IOHelper.copy(jarStream, os);
                        os.flush();
                    } finally {
                        os.close();
                    }
                    jarFile.deleteOnExit();
                }
                classpath.add(jarFile.toURI().toURL());
            }
        } finally {
            is.close();
        }
    }
}
