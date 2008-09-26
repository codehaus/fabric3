/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.tests.binding.axis2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import javax.activation.DataHandler;
import javax.activation.DataSource;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.osoa.sca.ServiceUnavailableException;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
public class Axis2EchoTest extends TestCase {

    @Reference
    protected Axis2EchoService service;

    @Reference
    protected Axis2FaultService faultService;

    private OMFactory factory;

    public void testEchoTextNoSecurity() {
        OMElement message = getInputText();
        OMElement response = service.echoNoSecurity(message);
        verifyOutputText(response);
    }

    public void testEchoTextWs() {
        OMElement message = getInputText();
        OMElement response = service.echoWs(message);
        verifyOutputText(response);
    }

    public void testEchoDataWsWithMTOM() throws IOException {
        OMElement message = getInputMtom();
        OMElement response = service.echoWs(message);
        verifyOutputMtom(response);
    }

    public void testEchoDataWithMTOMNoSecurity() throws IOException {
        OMElement message = getInputMtom();
        OMElement response = service.echoNoSecurity(message);
        verifyOutputMtom(response);
    }

    public void testRuntimeFault() {
        try {
            faultService.runtimeFaultOperation(getInputText());
            fail();
        } catch (ServiceUnavailableException e) {
            assertTrue(e.getMessage().contains("Runtime exception thrown from service"));
        }

    }

    private OMElement getInputText() {

        OMElement message = factory.createOMElement("data", null);
        OMText text = factory.createOMText(message, "Hello World");
        message.addChild(text);

        return message;

    }

    private void verifyOutputText(OMElement response) {
        String responseText = response.getText();
        assertEquals("Hello World", responseText);
    }

    private OMElement getInputMtom() {

        DataHandler dataHandler = new DataHandler(new DataSource() {
            public String getContentType() {
                return "text/dat";
            }

            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream("Hello World".getBytes());
            }

            public String getName() {
                return null;
            }

            public OutputStream getOutputStream() throws IOException {
                return null;
            }
        });

        OMElement message = factory.createOMElement("data", null);
        OMText text = factory.createOMText(dataHandler, true);
        text.setOptimize(true);
        message.addChild(text);

        return message;

    }

    private void verifyOutputMtom(OMElement response) throws IOException {

        OMText responseText = (OMText) response.getFirstOMChild();
        responseText.setOptimize(true);
        DataHandler responseData = (DataHandler) responseText.getDataHandler();
        InputStream is = responseData.getInputStream();
        InputStreamReader reader = new InputStreamReader(is);
        char buffer[] = new char[1024];
        StringWriter writer = new StringWriter();
        for (int count; (count = reader.read(buffer, 0, buffer.length)) > 0;) {
            writer.write(buffer, 0, count);

        }
        assertEquals("Hello World", writer.toString());

    }

    protected void setUp() throws Exception {
        super.setUp();
        factory = OMAbstractFactory.getOMFactory();
    }

}
