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
package org.fabric3.ws.mtom.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.fabric3.ws.mtom.DataExchangeService;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class DataExchangeTest extends TestCase {
    
    private DataExchangeService dataExchangeService;
    
    @Reference
    public void setDataExchangeService(DataExchangeService dataExchangeService) {
        this.dataExchangeService = dataExchangeService;
    }
    
    public void testExchange() throws Exception {
        
        DataHandler dataHandler = new DataHandler(new DataSource() {
            public String getContentType() {
                return "text/dat";
            }
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream("some dump data".getBytes());
            }
            public String getName() {
                return null;
            }
            public OutputStream getOutputStream() throws IOException {
                return null;
            }            
        });
        
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMElement parameter = fac.createOMElement("data", null);
        
        OMText binaryData = fac.createOMText(dataHandler, true);
        binaryData.setOptimize(true);
        parameter.addChild(binaryData);
        
        OMElement response = dataExchangeService.exchange(parameter);
        String responseText = response.getFirstElement().getText();
        assertEquals("some dump data acknowledged", responseText);
        
    }

}
