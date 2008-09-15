/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.contribution.war;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.services.contribution.ClasspathProcessor;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;

/**
 * Creates a classpath based on the contents of a WAR. Specifically, adds jars contained in WEB-INF/lib and classes in WEB-INF/classes to the
 * classpath.
 *
 * @version $Rev: 2450 $ $Date: 2008-01-10 14:09:41 -0800 (Thu, 10 Jan 2008) $
 */
@EagerInit
public class WarClasspathProcessor implements ClasspathProcessor {
    private static final Random RANDOM = new Random();

    private final ClasspathProcessorRegistry registry;

    public WarClasspathProcessor(@Reference ClasspathProcessorRegistry registry) {
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
        return name.endsWith(".war");
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
        dir.mkdir();
        InputStream is = jar.openStream();
        try {
            JarInputStream jarStream = new JarInputStream(is);
            JarEntry entry;
            File classesDir = null;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String path = entry.getName();
                if (path.startsWith("WEB-INF/lib/")) {
                    // expand jars in WEB-INF/lib and add to the classpath
                    File jarFile = File.createTempFile("fabric3", ".jar", dir);
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(jarFile));
                    try {
                        copy(jarStream, os);
                        os.flush();
                    } finally {
                        os.close();
                    }
                    jarFile.deleteOnExit();
                    classpath.add(jarFile.toURI().toURL());
                } else if (path.startsWith("WEB-INF/classes/")) {
                    // expand classes in WEB-INF/classes and add to classpath
                    if (classesDir == null) {
                        classesDir = new File(dir, "webclasses" + RANDOM.nextInt());
                        classesDir.mkdir();
                        classesDir.deleteOnExit();
                    }
                    int lastDelimeter = path.lastIndexOf("/");
                    String name = path.substring(lastDelimeter);
                    File pathAndPackageName = new File(classesDir, path.substring(16, lastDelimeter)); // 16 is length of "WEB-INF/classes
                    pathAndPackageName.mkdirs();
                    pathAndPackageName.deleteOnExit();
                    File classFile = new File(pathAndPackageName, name);
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(classFile));
                    try {
                        copy(jarStream, os);
                        os.flush();
                    } finally {
                        os.close();
                    }
                    classFile.deleteOnExit();
                    classpath.add(classesDir.toURI().toURL());
                }
            }
        } finally {
            is.close();
        }
    }

    private int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

}