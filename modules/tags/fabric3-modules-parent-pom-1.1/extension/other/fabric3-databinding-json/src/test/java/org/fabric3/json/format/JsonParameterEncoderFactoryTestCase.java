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
package org.fabric3.json.format;

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
public class JsonParameterEncoderFactoryTestCase extends TestCase {

    public void testSerializeDeserializeString() throws Exception {
        JsonParameterEncoderFactory factory = new JsonParameterEncoderFactory();

        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName("test");
        operation.addParameter(String.class.getName());
        operation.setReturnType(Void.class.getName());

        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.expect(chain.getPhysicalOperation()).andReturn(operation);
        List<InvocationChain> chains = new ArrayList<InvocationChain>();
        chains.add(chain);
        Wire wire = EasyMock.createMock(Wire.class);
        EasyMock.expect(wire.getInvocationChains()).andReturn(chains);
        EasyMock.replay(chain, wire);

        Message message = new MessageImpl();
        message.setBody(new Object[]{"test"});

        ParameterEncoder encoder = factory.getInstance(wire, getClass().getClassLoader());
        String serialized = encoder.encodeText(message);
        assertEquals("test", encoder.decode("test", serialized));
    }

    public void testSerializeDeserializeNull() throws Exception {
        JsonParameterEncoderFactory factory = new JsonParameterEncoderFactory();

        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName("test");
        operation.setReturnType(Void.class.getName());

        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.expect(chain.getPhysicalOperation()).andReturn(operation);
        List<InvocationChain> chains = new ArrayList<InvocationChain>();
        chains.add(chain);
        Wire wire = EasyMock.createMock(Wire.class);
        EasyMock.expect(wire.getInvocationChains()).andReturn(chains);
        EasyMock.replay(chain, wire);

        Message message = new MessageImpl();

        ParameterEncoder encoder = factory.getInstance(wire, getClass().getClassLoader());
        String serialized = encoder.encodeText(message);
        assertNull(encoder.decode("test", serialized));
    }

    public void testSerializeDeserializeObject() throws Exception {
        JsonParameterEncoderFactory factory = new JsonParameterEncoderFactory();

        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName("test");
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

        ParameterEncoder encoder = factory.getInstance(wire, getClass().getClassLoader());
        String serialized = encoder.encodeText(message);
        assertTrue(encoder.decode("test", serialized) instanceof Foo);
    }

    public void testSerializeDeserializeException() throws Exception {
        JsonParameterEncoderFactory factory = new JsonParameterEncoderFactory();

        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName("test");
        operation.addFaultType(FooException.class.getName());
        operation.setReturnType(Void.class.getName());

        InvocationChain chain = EasyMock.createMock(InvocationChain.class);
        EasyMock.expect(chain.getPhysicalOperation()).andReturn(operation);
        List<InvocationChain> chains = new ArrayList<InvocationChain>();
        chains.add(chain);
        Wire wire = EasyMock.createMock(Wire.class);
        EasyMock.expect(wire.getInvocationChains()).andReturn(chains);
        EasyMock.replay(chain, wire);

        Message message = new MessageImpl();
        FooException fault = new FooException("test");
        message.setBodyWithFault(fault);

        ParameterEncoder encoder = factory.getInstance(wire, getClass().getClassLoader());
        String serialized = encoder.encodeText(message);
        FooException e = (FooException) encoder.decodeFault("test", serialized);
        assertEquals("test", e.getMessage());

    }


    private static class Foo {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class FooException extends Exception {
        private static final long serialVersionUID = 4937174167807498685L;

        public FooException(String message) {
            super(message);
        }
    }
}
