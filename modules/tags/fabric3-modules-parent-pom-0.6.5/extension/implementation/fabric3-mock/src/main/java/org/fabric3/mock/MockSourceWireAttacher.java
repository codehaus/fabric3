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
package org.fabric3.mock;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 */
public class MockSourceWireAttacher implements SourceWireAttacher<MockWireSourceDefinition> {

    public void attachObjectFactory(MockWireSourceDefinition arg0, ObjectFactory<?> arg1, PhysicalWireTargetDefinition definition) throws WiringException {
        // Empty implementation; we don't want to attach anything to the mock
    }

    public void attachToSource(MockWireSourceDefinition arg0, PhysicalWireTargetDefinition arg1, Wire arg2)
            throws WiringException {
        // Empty implementation; we don't want to attach anything to the mock
    }

    public void detachFromSource(MockWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {

    }
}
