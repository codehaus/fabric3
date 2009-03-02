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
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.fabric3.binding.ws.provision.WsdlElement;
import org.fabric3.model.type.service.JavaServiceContract;
import org.fabric3.spi.generator.GenerationException;

import org.xml.sax.SAXException;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;

public class DefaultWsdlElementParserTest extends TestCase {

    public void testWithWsdlElement() throws GenerationException {
        String element = "urn:weather#wsdl.port(WeatherService/WeatherPort)";
        WsdlElement wsdlElement = new DefaultWsdlElementParser().parseWsdlElement(element, null, null);
        assertEquals(new QName("urn:weather", "WeatherService"), wsdlElement.getServiceName());
        assertEquals(new QName("urn:weather", "WeatherPort"), wsdlElement.getPortName());
    }

    public void testWithServiceContract() throws GenerationException {
        JavaServiceContract javaServiceContract = new JavaServiceContract(TestService.class);
        WsdlElement wsdlElement = new DefaultWsdlElementParser().parseWsdlElement(null, null, javaServiceContract);        
        assertEquals(new QName("org.fabric3.binding.ws.metro.control", "TestServiceService"), wsdlElement.getServiceName());
        assertEquals(new QName("org.fabric3.binding.ws.metro.control", "TestServicePort"), wsdlElement.getPortName());
    }

    public void testWithWsdlLocation() throws GenerationException, IOException, XMLStreamException, SAXException {
        URL wsdlLocation = getClass().getClassLoader().getResource("Weather.wsdl");
        WSDLModel wsdlModel = new DefaultWsdlParser().parse(wsdlLocation);
        WsdlElement wsdlElement = new DefaultWsdlElementParser().parseWsdlElement(null, wsdlModel, null);
        assertEquals(new QName("urn:weather", "WeatherService"), wsdlElement.getServiceName());
        assertEquals(new QName("urn:weather", "WeatherPort"), wsdlElement.getPortName());
    }

}
