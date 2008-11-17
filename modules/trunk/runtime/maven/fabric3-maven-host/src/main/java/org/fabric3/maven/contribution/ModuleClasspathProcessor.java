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

import org.fabric3.maven.MavenHostInfo;
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
        if ("file".equals(url.getProtocol())) {
            // assume exploded directories are maven modules
            return true;
        }
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
