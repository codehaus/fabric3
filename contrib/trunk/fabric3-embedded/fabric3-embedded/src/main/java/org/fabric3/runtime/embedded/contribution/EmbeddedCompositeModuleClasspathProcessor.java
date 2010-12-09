/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.runtime.embedded.contribution;

import org.fabric3.runtime.embedded.api.EmbeddedComposite;
import org.fabric3.spi.contribution.archive.ClasspathProcessor;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Fabricates a classpath for a Maven module by including the classes and test-classes directories and any module dependencies.
 *
 * @version $Rev: 7899 $ $Date: 2009-11-23 10:44:11 +0100 (Mon, 23 Nov 2009) $
 */
@EagerInit
public class EmbeddedCompositeModuleClasspathProcessor implements ClasspathProcessor {
    private ClasspathProcessorRegistry registry;

    public EmbeddedCompositeModuleClasspathProcessor(@Reference ClasspathProcessorRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public boolean canProcess(URL url) {
        if (EmbeddedComposite.EMBEDDED_COMPOSITE.equals(url.getProtocol())) {
            // assume exploded directories are maven modules
            return true;
        }
        try {
            URLConnection conn = url.openConnection();
            return EmbeddedComposite.CONTENT_TYPE_CLASSPATH.equals(conn.getContentType());
        } catch (IOException e) {
            return false;
        }
    }

    public List<URL> process(URL url) throws IOException {
        String file = url.getFile();
        List<URL> urls = new ArrayList<URL>(2);
        urls.add(new File(file, "classes").toURI().toURL());
        urls.add(new File(file, "test-classes").toURI().toURL());
//        urls.addAll(hostInfo.getDependencyUrls());
        return urls;
    }
}
