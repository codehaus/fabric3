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
package org.fabric3.jaxb.format;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
public class JAXBParameterEncoderFactoryTestCase extends TestCase {

    public void testSerializeDeserialize() throws Exception {
        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.addParameter(Foo.class.getName());
        operation.setReturnType(Void.class.getName());

        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.expect(chain.getPhysicalOperation()).andReturn(operation);
        List<InvocationChain> chains = new ArrayList<InvocationChain>();
        chains.add(chain);
        Wire wire = EasyMock.createMock(Wire.class);
        EasyMock.expect(wire.getInvocationChains()).andReturn(chains);
        EasyMock.replay(chain, wire);

        Message message = new MessageImpl();
        message.setBody(new Object[]{new Foo()});
        JAXBParameterEncoderFactory factory = new JAXBParameterEncoderFactory();
        ParameterEncoder encoder = factory.getInstance(wire, getClass().getClassLoader());
        String serialized = encoder.encodeText(message);
        Object deserialized = encoder.decode("", serialized);
        assertTrue(deserialized instanceof Foo);
    }

}
