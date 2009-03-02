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
package org.fabric3.fabric.builder.classloader;

import java.net.URI;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.builder.classloader.ClassLoaderWireBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.model.physical.PhysicalClassLoaderWireDefinition;

/**
 * @version $Revision$ $Date$
 */
public class ClassLoaderWireBuilderImpl implements ClassLoaderWireBuilder {
    private ClassLoaderRegistry registry;

    public ClassLoaderWireBuilderImpl(@Reference ClassLoaderRegistry registry) {
        this.registry = registry;
    }

    public void build(MultiParentClassLoader source, PhysicalClassLoaderWireDefinition wireDefinition) {
        URI uri = wireDefinition.getTargetClassLoader();
        ClassLoader target = registry.getClassLoader(uri);
        if (target == null) {
            throw new AssertionError("Target classloader not found: " + uri);
        }
        String packageName = wireDefinition.getPackageName();
        if (packageName != null) {
            ClassLoader filter = new ClassLoaderWireFilter(target, packageName);
            source.addParent(filter);
        } else {
            source.addParent(target);
        }
    }
}
