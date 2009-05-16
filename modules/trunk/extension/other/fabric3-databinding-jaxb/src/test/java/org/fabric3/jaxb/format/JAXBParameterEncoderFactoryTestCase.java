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
 * @version $Revision$ $Date$
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
