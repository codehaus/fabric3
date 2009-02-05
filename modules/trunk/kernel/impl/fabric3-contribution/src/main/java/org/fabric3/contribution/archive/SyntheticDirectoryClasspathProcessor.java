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
package org.fabric3.contribution.archive;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.contribution.archive.ClasspathProcessor;
import org.fabric3.spi.contribution.archive.ClasspathProcessorRegistry;
import org.fabric3.util.io.FileHelper;

/**
 * Creates the classpath for a contribution synthesized from a directory. All contained jars will be added to the classpath.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class SyntheticDirectoryClasspathProcessor implements ClasspathProcessor {

    private final ClasspathProcessorRegistry registry;

    public SyntheticDirectoryClasspathProcessor(@Reference ClasspathProcessorRegistry registry) {
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
        if (!"file".equals(url.getProtocol())) {
            return false;
        }
        File root = FileHelper.toFile(url);
        return root.isDirectory();
    }

    public List<URL> process(URL url) throws IOException {
        List<URL> classpath = new ArrayList<URL>();
        File root = FileHelper.toFile(url);
        for (File file : root.listFiles()) {
            if (file.getName().endsWith(".jar")) {
                classpath.add(file.toURI().toURL());
            }
        }
        return classpath;
    }


}