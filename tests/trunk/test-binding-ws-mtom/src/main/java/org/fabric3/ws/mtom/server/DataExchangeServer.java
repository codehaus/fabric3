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
package org.fabric3.ws.mtom.server;

import java.io.InputStream;

import javax.activation.DataHandler;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.fabric3.ws.mtom.DataExchangeService;

/**
 * @version $Revision$ $Date$
 */
public class DataExchangeServer implements DataExchangeService {

    public OMElement exchange(OMElement omElement) throws Exception {
        
        try {
            
            OMElement soapBody = omElement.getFirstElement();
            OMElement operation = soapBody.getFirstElement();
            OMElement parameter = operation.getFirstElement();
            
            OMText binaryNode = (OMText) parameter.getFirstOMChild();
            binaryNode.setOptimize(true);
            DataHandler dataHandler = (DataHandler) binaryNode.getDataHandler();
            
            InputStream in = dataHandler.getDataSource().getInputStream();
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);
            
            String data = new String(buffer, 0, read);
            
            OMFactory fac = OMAbstractFactory.getOMFactory();
    
            OMElement resp = fac.createOMElement("achnowledgement", null);
    
            resp.setText(data + " acknowledged");
    
            return resp;
            
        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        
    }

}
