/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.tests.binding.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMText;
import org.osoa.sca.annotations.Reference;

/**
 *
 * @version $Revision$ $Date$
 */
public class FtpDataTransferServiceImpl implements FtpDataTransferService {
    
    @Reference protected WsDataTransferService wsDataTransferService;

    public void transferData(String fileName, InputStream data) throws Exception {
        try {
            OMElement wrapper = createWrapper(fileName, data);
            wsDataTransferService.transferData(wrapper);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private OMElement createWrapper(final String fileName, final InputStream data) {
        
        OMFactory factory = OMAbstractFactory.getOMFactory();;
        
        DataHandler dataHandler = new DataHandler(new DataSource() {
            public String getContentType() {
                return "text/dat";
            }
            public InputStream getInputStream() throws IOException {
                return data;
            }
            public String getName() {
                return fileName;
            }
            public OutputStream getOutputStream() throws IOException {
                return null;
            }
        });
        
        OMElement wrapper = factory.createOMElement("wrapper", null);
        
        OMElement fileElement = factory.createOMElement("fileName", null);
        fileElement.addChild(factory.createOMText(fileName));

        OMElement dataElement = factory.createOMElement("data", null);
        OMText text = factory.createOMText(dataHandler, true);
        text.setOptimize(true);
        dataElement.addChild(text);
        
        wrapper.addChild(fileElement);
        wrapper.addChild(dataElement);
        
        return wrapper;
        
    }

}
