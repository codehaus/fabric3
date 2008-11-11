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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.xml.namespace.QName;

import org.fabric3.binding.ws.metro.provision.WsdlElement;
import org.fabric3.spi.generator.GenerationException;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;

import junit.framework.TestCase;

public class DefaultAddressResolverTest extends TestCase {
    
    private AddressResolver addressResolver = new DefaultAddressResolver();
    private WsdlParser wsdlParser = new DefaultWsdlParser();

    public void testResolveServiceAddressWithTargetUriSuccess() throws GenerationException {
        URI targetUri = URI.create("/weather");
        assertEquals(targetUri, addressResolver.resolveServiceAddress(targetUri, null, null));
    }

    public void testResolveReferenceAddressWithTargetUriSuccess() throws GenerationException, MalformedURLException {
        URI targetUri = URI.create("http://localhost:7001/weather,http://localhost:7002/weather");
        URL[] urls = addressResolver.resolveReferenceAddress(targetUri, null, null);
        assertEquals(2, urls.length);
        assertEquals(new URL("http://localhost:7001/weather"), urls[0]);
        assertEquals(new URL("http://localhost:7002/weather"), urls[1]);
    }

    public void testResolveServiceAddressWithWsdlElementSuccess() throws GenerationException {
        WsdlElement wsdlElement = new WsdlElement(new QName("urn:weather", "WeatherService"), new QName("urn:weather", "WeatherPort"));
        WSDLModel wsdlModel = wsdlParser.parse(getClass().getClassLoader().getResource("WeatherServerSide.wsdl"));
        assertEquals( URI.create("/weather"), addressResolver.resolveServiceAddress(null, wsdlElement, wsdlModel));
    }

    public void testResolveReferenceAddressWithWsdlElementSuccess() throws GenerationException, MalformedURLException {
        WsdlElement wsdlElement = new WsdlElement(new QName("urn:weather", "WeatherService"), new QName("urn:weather", "WeatherPort"));
        WSDLModel wsdlModel = wsdlParser.parse(getClass().getClassLoader().getResource("WeatherClientSide.wsdl"));
        URL[] urls = addressResolver.resolveReferenceAddress(null, wsdlElement, wsdlModel);
        assertEquals(2, urls.length);
        assertEquals(new URL("http://localhost:7001/weather"), urls[0]);
        assertEquals(new URL("http://localhost:7002/weather"), urls[1]);
    }

}
