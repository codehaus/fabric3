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
package org.fabric3.binding.tcp.runtime.wire;

import org.fabric3.binding.tcp.provision.TCPWireTargetDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * TODO: TCP binding for Reference yet to be implemented.
 * @version $Revision$ $Date$
 */
public class TCPTargetWireAttacher implements TargetWireAttacher<TCPWireTargetDefinition> {

    public void attachToTarget(PhysicalWireSourceDefinition source, TCPWireTargetDefinition target, Wire wire) throws WiringException {

        new UnsupportedOperationException("TCP binding for Reference yet to be implemented");
    }

    public void detachFromTarget(PhysicalWireSourceDefinition source, TCPWireTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public ObjectFactory<?> createObjectFactory(TCPWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

}
