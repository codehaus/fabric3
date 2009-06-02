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
package org.fabric3.json.format.jsonrpc;

import junit.framework.TestCase;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;

/**
 * @version $Revision$ $Date$
 */
public class JsonRpcMessageEncoderTestCase extends TestCase {

//    public void testEncode() throws Exception {
//        // don't compare id
//        String expected = "{\"jsonrpc\":\"2.0\",\"params\":[\"one\",\"two\"],\"method\":\"test\",\"id\":\"";
//        JsonRpcMessageEncoder encoder = new JsonRpcMessageEncoder();
//        Message message = new MessageImpl();
//        message.setBody(new Object[]{"one", "two"});
//        String encoded = encoder.encodeText("test", message, null);
//        assertTrue(encoded.startsWith(expected));
//        assertTrue(encoded.endsWith("\"}"));
//    }
//
//    public void testEncodeComplexType() throws Exception {
//        // don't compare id
//        String expected = "{\"jsonrpc\":\"2.0\",\"params\":[{\"firstName\":\"first\",\"lastName\":\"last\"}],\"method\":\"test\",\"id\":\"";
//        JsonRpcMessageEncoder encoder = new JsonRpcMessageEncoder();
//        Message message = new MessageImpl();
//        Foo foo = new Foo();
//        foo.setFirstName("first");
//        foo.setLastName("last");
//        message.setBody(new Object[]{foo});
//        String encoded = encoder.encodeText("test", message, null);
//        assertTrue(encoded.startsWith(expected));
//        assertTrue(encoded.endsWith("\"}"));
//    }
//
//    public void testEncodeResponse() throws Exception {
//        String expected = "{\"jsonrpc\":\"2.0\",\"result\":\"response\",\"id\":\"\"}";
//        JsonRpcMessageEncoder encoder = new JsonRpcMessageEncoder();
//        Message message = new MessageImpl();
//        message.setBody("response");
//        String encoded = encoder.encodeResponseText("test", message, null);
//        assertEquals(expected, encoded);
//    }
//
    public void testDecodeComplexType() throws Exception {
        String encoded = "{\"jsonrpc\":\"2.0\",\"params\":[{\"firstName\":\"first\",\"lastName\":\"last\"}],\"method\":\"test\",\"id\":\"1\"}";
        JsonRpcMessageEncoder encoder = new JsonRpcMessageEncoder();
        Message message = encoder.decode(encoded, null);
    }

    private class Foo {
        private String firstName;
        private String lastName;


        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }


}
