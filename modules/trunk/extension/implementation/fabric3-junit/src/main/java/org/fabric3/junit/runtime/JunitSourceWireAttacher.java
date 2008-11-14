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
package org.fabric3.junit.runtime;

import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.junit.provision.JUnitWireSourceDefinition;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class JunitSourceWireAttacher implements SourceWireAttacher<JUnitWireSourceDefinition> {
    private WireHolder holder;

    public JunitSourceWireAttacher(@Reference WireHolder holder) {
        this.holder = holder;
    }

    public void attachToSource(JUnitWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        final String testName = source.getTestName();
        holder.put(testName, wire);
    }

    public void attachObjectFactory(JUnitWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target)
            throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void detachFromSource(JUnitWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void detachObjectFactory(JUnitWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }
}
