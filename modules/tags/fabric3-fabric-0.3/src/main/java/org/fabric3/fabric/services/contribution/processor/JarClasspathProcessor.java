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

import java.io.File;
import static java.io.File.separator;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.services.archive.JarService;
import org.fabric3.spi.services.contribution.ClasspathProcessor;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;

/**
 * Creates a classpath based on the contents of a jar. Specifically, adds the jar and any zip/jar archives found in
 * META-INF/lib to the classpath
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JarClasspathProcessor implements ClasspathProcessor {
    private JarService jarService;
    private File tempDir;


    public JarClasspathProcessor(@Reference ClasspathProcessorRegistry registry, @Reference JarService jarService) {
        this.jarService = jarService;
        String tmp = AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty("user.home") + separator + ".fabric3" + separator + "tmp";
            }
        });
        tempDir = new File(tmp);
        registry.register(this);
    }

    public boolean canProcess(URL url) {
        String name = url.getFile().toLowerCase();
        return name.endsWith(".jar") || name.endsWith(".zip");
    }

    public List<URL> process(URL url) throws IOException {
        List<URL> classpath = new ArrayList<URL>();
        // add the the jar itself to the classpath
        classpath.add(url);
        // expand contents of the lib directory (if it exists) and add the expanded content
        String file = url.getFile();
        String name = URLEncoder.encode(file.substring(file.lastIndexOf("/") + 1), "UTF-8");
        File expandedDir = generateTempDir(name);
        jarService.expand(url, expandedDir, true);
        File libDir = new File(expandedDir, "META-INF/lib");
        File[] libraries = libDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jar"));
            }
        });
        if (libraries != null) {
            for (File library : libraries) {
                classpath.add(library.toURI().toURL());
            }
        }
        return classpath;
    }

    private File generateTempDir(String name) {
        File expandedDir = new File(tempDir, name);
        int i = 1;
        while (expandedDir.exists()) {
            expandedDir = new File(tempDir, name + String.valueOf(i));
            ++i;
        }
        return expandedDir;
    }

}
