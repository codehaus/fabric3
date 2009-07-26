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
package org.fabric3.fabric.lcm;

import java.net.URI;

import org.fabric3.model.type.component.Autowire;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.spi.lcm.LogicalComponentStore;
import org.fabric3.spi.lcm.ReadException;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

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