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
package org.fabric3.binding.ws.axis2.runtime;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.axis2.provision.Axis2WireSourceDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 *          <p/>
 *          TODO Add support for WSDL contract
 */
@EagerInit
public class Axis2SourceWireAttacher implements SourceWireAttacher<Axis2WireSourceDefinition> {

    @Reference protected Axis2ServiceProvisioner serviceProvisioner;
    @Reference protected ClassLoaderRegistry classLoaderRegistry;

    public void attachToSource(Axis2WireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        serviceProvisioner.provision(source, wire);
    }

    public void detachFromSource(Axis2WireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new AssertionError();        
    }

    public void attachObjectFactory(Axis2WireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition definition) throws WiringException {
        throw new AssertionError();
    }

    public void detachObjectFactory(Axis2WireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

}
