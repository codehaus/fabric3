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
package org.fabric3.fabric.services.lcm;

import java.net.URI;

import org.fabric3.model.type.component.Autowire;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.services.lcm.LogicalComponentStore;
import org.fabric3.spi.services.lcm.ReadException;

/**
 * A non-persistent LogicalComponentStore.
 *
 * @version $Rev$ $Date$
 */
public class TransientLogicalComponentStore implements LogicalComponentStore {
    private URI domainUri;
    private Autowire autowire = Autowire.OFF;

    public TransientLogicalComponentStore(URI domainUri, Autowire autowire) {
        this.domainUri = domainUri;
        this.autowire = autowire;
    }

    protected TransientLogicalComponentStore() {
    }

    public LogicalCompositeComponent read() throws ReadException {
        Composite type = new Composite(null);
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>(domainUri.toString());
        definition.setImplementation(impl);
        type.setAutowire(autowire);
        return new LogicalCompositeComponent(domainUri, definition, null);
    }

    public void store(LogicalCompositeComponent domain) {
        // no op
    }

    protected void setDomainUri(URI domainUri) {
        this.domainUri = domainUri;
    }

    protected void setAutowire(Autowire autowire) {
        this.autowire = autowire;
    }
}