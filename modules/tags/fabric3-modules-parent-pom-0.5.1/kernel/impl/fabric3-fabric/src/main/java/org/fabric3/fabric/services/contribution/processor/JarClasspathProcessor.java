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
package org.fabric3.fabric.services.contribution.processor;

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

import org.fabric3.fabric.util.IOHelper;
import org.fabric3.spi.services.contribution.ClasspathProcessor;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;

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
        dir.mkdir();
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
                File jarFile = File.createTempFile("fabric3", ".jar", dir);
                OutputStream os = new BufferedOutputStream(new FileOutputStream(jarFile));
                try {
                    IOHelper.copy(jarStream, os);
                    os.flush();
                } finally {
                    os.close();
                }
                jarFile.deleteOnExit();
                classpath.add(jarFile.toURI().toURL());
            }
        } finally {
            is.close();
        }
    }
}
