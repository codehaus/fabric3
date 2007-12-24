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
package org.fabric3.fabric.services.domain;

import java.net.URI;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.scdl.Autowire;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.runtime.assembly.LogicalComponentStore;
import org.fabric3.spi.runtime.assembly.RecoveryException;

/**
 * A non-persistent LogicalComponentStore
 *
 * @version $Rev$ $Date$
 */
public class NonPersistentLogicalComponentStore implements LogicalComponentStore {
    private URI domainUri;
    private Autowire autowire = Autowire.OFF;

    public NonPersistentLogicalComponentStore(URI domainUri, Autowire autowire) {
        this.domainUri = domainUri;
        this.autowire = autowire;
    }

    @Constructor
    public NonPersistentLogicalComponentStore(@Reference HostInfo info) {
        domainUri = info.getDomain();
    }

    public LogicalComponent<CompositeImplementation> read() throws RecoveryException {
        Composite type = new Composite(null);
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>(domainUri.toString());
        definition.setImplementation(impl);
        type.setAutowire(autowire);
        return new LogicalComponent<CompositeImplementation>(domainUri, domainUri, definition, null);
    }

    public void store(LogicalComponent<CompositeImplementation> domain) {
        // no op
    }
}
