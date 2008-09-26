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
package org.fabric3.tests.interop;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.osoa.sca.annotations.Reference;

import org.fabric3.tests.interop.axis2.Axis2EchoService;

/**
 * @version $Rev$ $Date$
 */
public class AxisToCXFTest extends TestCase {
    @Reference
    protected Axis2EchoService service;
    private OMFactory factory;

    public void testAxisToCXFString() {
        OMElement message = factory.createOMElement("data", null);
        OMText text = factory.createOMText(message, "Hello World");
        message.addChild(text);

        OMElement response = service.echoString(message);
        String responseText = response.getFirstElement().getText();
        assertEquals("Hello World", responseText);
    }

    public void testAxisToCXFInt() {
        OMElement message = factory.createOMElement("data", null);
        OMText text = factory.createOMText(message, "12345");
        message.addChild(text);

        OMElement response = service.echoString(message);
        String responseText = response.getFirstElement().getText();
        assertEquals("12345", responseText);
    }

    protected void setUp() throws Exception {
        super.setUp();
        factory = OMAbstractFactory.getOMFactory();
    }
}
