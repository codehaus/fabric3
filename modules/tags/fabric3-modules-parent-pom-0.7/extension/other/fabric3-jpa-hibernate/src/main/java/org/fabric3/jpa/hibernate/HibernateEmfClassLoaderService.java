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
package org.fabric3.jpa.hibernate;

import java.net.URI;

import org.osoa.sca.annotations.Reference;

import org.fabric3.jpa.spi.classloading.EmfClassLoaderService;
import org.fabric3.spi.classloader.ClassLoaderRegistry;

/**
 * Returns the previously constructed classloader. The Hibernate extension implicitly imports the Hibernate extension contribution into any
 * application using it, so nothing is required to be done here.
 *
 * @version $Revision$ $Date$
 */
public class HibernateEmfClassLoaderService implements EmfClassLoaderService {
    private ClassLoaderRegistry classLoaderRegistry;

    public HibernateEmfClassLoaderService(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public ClassLoader getEmfClassLoader(URI classLoaderUri) {
        return classLoaderRegistry.getClassLoader(classLoaderUri);
    }
}
