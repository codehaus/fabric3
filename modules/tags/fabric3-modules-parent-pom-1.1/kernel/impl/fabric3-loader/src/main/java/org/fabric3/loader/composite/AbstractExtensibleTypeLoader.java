/*
 * Fabric3
 * Copyright © 2008-2009 Metaform Systems Limited
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
package org.fabric3.loader.composite;

import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;

import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.TypeLoader;

/**
 * TypeLoader implementation that can delegate back to the LoaderRegistry to process sub-elements in a composite.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public abstract class AbstractExtensibleTypeLoader<T> implements TypeLoader<T> {
    protected LoaderRegistry registry;

    protected AbstractExtensibleTypeLoader(LoaderRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.registerLoader(getXMLType(), this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(getXMLType());
    }

    /**
     * Returns the QName of the type this implementation loads.
     *
     * @return the QName of the type this implementation loads
     */
    public abstract QName getXMLType();

}
