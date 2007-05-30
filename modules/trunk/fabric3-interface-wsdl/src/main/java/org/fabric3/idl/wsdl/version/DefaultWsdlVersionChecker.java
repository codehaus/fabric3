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

package org.fabric3.idl.wsdl.version;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Default implementation of the WSDL version checker.
 */
public class DefaultWsdlVersionChecker implements WsdlVersionChecker {
    
    /**
     * WSDL 2.0 Namespace.
     */
    private static final String WSDL_20_NS = "http://www.w3.org/ns/wsdl";

    /**
     * @see org.fabric3.idl.wsdl.version.WsdlVersionChecker#getVersion(java.io.InputStream)
     */
    public WsdlVersion getVersion(InputStream wsdlStream) {
        
        try {
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(wsdlStream);
            while(true) {
                switch(reader.next()) {
                    case START_ELEMENT:
                        String nsUri = reader.getName().getNamespaceURI();
                        return nsUri.equals(WSDL_20_NS) ? WsdlVersion.VERSION_1_1 : WsdlVersion.VERSION_2_0;
                }
            }
        } catch(XMLStreamException ex) {
            throw new WsdlVersionCheckerException("Unable to read stream", ex);
        }
        
    }

}
