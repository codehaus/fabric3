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
import org.oasisopen.sca.ServiceUnavailableException;
import org.oasisopen.sca.annotation.Reference;

/**
 * @version $Rev$ $Date$
 */
public class Axis2EchoTest extends TestCase {

    @Reference
    protected Axis2EchoService service;

    @Reference
    protected Axis2FaultService faultService;

    private OMFactory factory;

    /**
     * Test for simple WS call without Security
     */
    public void testEchoTextNoSecurity() {
        OMElement message = getInputText();
        OMElement response = service.echoNoSecurity(message);
        verifyOutputText(response);
    }

    /**
     * Test for simple WS call with UsernameToken Security
     */
    public void testEchoTextWs() {
        OMElement message = getInputText();
        OMElement response = service.echoWsUsernameToken(message);
        verifyOutputText(response);
    }

    /**
     * Test for simple WS call with X509Token Security
     */
    public void testEchoTextWsWithX509() {
        OMElement message = getInputText();
        OMElement response = service.echoWsX509Token(message);
        verifyOutputText(response);
    }

    /**
     * Test for MTOM WS call with UsernameToken Security
     *
     * @throws IOException thrown if error occurred in unmarshalling MTOM
     */
    public void testEchoDataWsWithMTOM() throws IOException {
        OMElement message = getInputMtom();
        OMElement response = service.echoWsUsernameToken(message);
        verifyOutputMtom(response);
    }

    /**
     * Test for MTOM WS call with X509Token Security
     *
     * @throws IOException thrown if error occurred in unmarshalling MTOM
     */
    public void testEchoDataWsWithX509MTOM() throws IOException {
        OMElement message = getInputMtom();
        OMElement response = service.echoWsX509Token(message);
        verifyOutputMtom(response);
    }

    /**
     * Test for MTOM WS call without Security.
     *
     * @throws IOException thrown if error occurred in unmarshalling MTOM
     */
    public void testEchoDataWithMTOMNoSecurity() throws IOException {
        OMElement message = getInputMtom();
        OMElement response = service.echoNoSecurity(message);
        verifyOutputMtom(response);
    }

    /**
     * Test for WS call generating runtime fault
     */
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
        assertTrue(text.isOptimized());
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
