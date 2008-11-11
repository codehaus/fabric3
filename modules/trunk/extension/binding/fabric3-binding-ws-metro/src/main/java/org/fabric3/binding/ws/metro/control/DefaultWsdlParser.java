/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ñLicenseî), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an ñas isî basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.binding.ws.metro.control;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import org.fabric3.spi.generator.GenerationException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;

/*
 * Default implementation of the WSDL parser.
 */
public class DefaultWsdlParser implements WsdlParser {
    
    /**
     * Parses a WSDL document to the information model.
     * @param wsdlLocation Location of the WSDL document.
     * @return WSDL model object.
     * 
     * @throws GenerationException If unable to parse the WSDL.
     */
    public WSDLModel parse(URL wsdlLocation) throws GenerationException {
        
        if (wsdlLocation == null) {
            return null;
        }
        
        InputStream inputStream = null;
        try {
            inputStream = wsdlLocation.openStream();
            return RuntimeWSDLParser.parse(wsdlLocation, new StreamSource(inputStream), entityResolver, false, (Container) null);
        } catch (XMLStreamException e) {
            throw new GenerationException(e);
        } catch (IOException e) {
            throw new GenerationException(e);
        } catch (SAXException e) {
            throw new GenerationException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new GenerationException(e);
            }
        }
        
    }
    
    /*
     * Entity resolution is not currently supported.
     */
    private EntityResolver entityResolver = new EntityResolver() {
        public InputSource resolveEntity(String systemId, String publicId) throws SAXException, IOException {
            return null;
        }
    };

}
