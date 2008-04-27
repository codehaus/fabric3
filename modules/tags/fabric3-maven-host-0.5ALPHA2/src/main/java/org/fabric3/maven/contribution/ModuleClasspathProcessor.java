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
package org.fabric3.maven.contribution;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.maven.runtime.MavenHostInfo;
import org.fabric3.spi.services.contribution.ClasspathProcessor;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;

/**
 * Fabricates a classpath for a Maven module by including the classes and test-classes directories and any module dependencies.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ModuleClasspathProcessor implements ClasspathProcessor {
    public static final String CONTENT_TYPE = "application/vnd.fabric3.maven-project";
    private ClasspathProcessorRegistry registry;
    private MavenHostInfo hostInfo;

    public ModuleClasspathProcessor(@Reference ClasspathProcessorRegistry registry, @Reference MavenHostInfo hostInfo) {
        this.registry = registry;
        this.hostInfo = hostInfo;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public boolean canProcess(URL url) {
        try {
            URLConnection conn = url.openConnection();
            return CONTENT_TYPE.equals(conn.getContentType());
        } catch (IOException e) {
            return false;
        }
    }

    public List<URL> process(URL url) throws IOException {
        String file = url.getFile();
        List<URL> urls = new ArrayList<URL>(2);
        urls.add(new File(file, "classes").toURI().toURL());
        urls.add(new File(file, "test-classes").toURI().toURL());
        urls.addAll(hostInfo.getDependencyUrls());
        return urls;
    }
}
