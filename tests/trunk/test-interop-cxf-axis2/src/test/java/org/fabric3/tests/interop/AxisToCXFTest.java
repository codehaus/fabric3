/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
