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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.services.classloading;

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import junit.framework.TestCase;

import org.fabric3.fabric.classloader.ClassLoaderRegistryImpl;
import org.fabric3.spi.classloader.ClassLoaderRegistry;

/**
 * @version $Rev$ $Date$
 */
public class ClassLoaderRegistryImplTestCase extends TestCase {
    private ClassLoaderRegistry registry = new ClassLoaderRegistryImpl();

    public void testResolveParentUris() throws Exception {
        URL[] urls = new URL[0];
        ClassLoader parent = new URLClassLoader(urls, null);
        ClassLoader loader = new URLClassLoader(urls, parent);
        URI parentId = URI.create("parent");
        registry.register(parentId, parent);
        URI loaderId = URI.create("loader");
        registry.register(loaderId, loader);
        List<URI> parents = registry.resolveParentUris(loader);
        assertEquals(1, parents.size());
        assertEquals(parentId, parents.get(0));
    }
}
