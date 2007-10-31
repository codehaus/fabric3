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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.fabric3.ws.mtom.DataExchangeService;
import org.osoa.sca.annotations.Reference;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class DataExchangeTest extends TestCase {
    
    private DataExchangeService dataExchangeService;
    
    @Reference
    public void setDataExchangeService(DataExchangeService dataExchangeService) {
        this.dataExchangeService = dataExchangeService;
    }
    
    public void testExchange() {
        
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMElement request = fac.createOMElement("data", null);
        request.setText("Some dump data");
        
        OMElement response = dataExchangeService.exchange(request);
        String result = response.getFirstElement().getText();
        
        System.err.println(response);
        
        assertEquals("Some dump data acknowledged", result);
        
    }

}
