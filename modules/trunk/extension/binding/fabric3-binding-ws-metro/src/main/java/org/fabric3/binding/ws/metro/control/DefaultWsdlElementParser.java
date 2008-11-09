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

import java.net.URL;

import javax.xml.namespace.QName;

import org.fabric3.binding.ws.metro.provision.WsdlElement;
import org.fabric3.introspection.impl.contract.JavaServiceContract;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.generator.GenerationException;

/**
 * 
 * Default implementation of the WSDL element parser.
 * 
 * TODO Currently support only WSDL 1.1 and also one WSDL 1.1 port
 *
 */
public class DefaultWsdlElementParser implements WsdlElementParser {
    
    /**
     * Parses the WSDL element.
     * 
     * @param wsdlElement String representation of the WSDL element.
     * @param wsdlLocation URL to the WSDL.
     * @param serviceContract Service contract for the WSDL.
     * @return Parsed WSDL element.
     * @throws GenerationException If unable to parse the WSDL element.
     */
    public WsdlElement parseWsdlElement(String wsdlElement, URL wsdlLocation, ServiceContract<?> serviceContract) throws GenerationException {
        
        // No wsdl element, just location, parse the WSDL location
        if (wsdlElement == null && wsdlLocation != null) {
            return parseWsdl(wsdlLocation);
        }
        
        // Wsdl element present, so parse it
        if (wsdlElement != null) {
            return parseWsdlElement(wsdlElement, wsdlLocation);
        }
        
        // No wsdl element or location, synthesize the names
        return synthesizeFromContract(serviceContract);
        
    }

    /*
     * Parses the service name and port name from the WSDL.
     */
    private WsdlElement parseWsdl(URL wsdlLocation) throws GenerationException {
        // TODO Support wsdl document parsing
        throw new GenerationException("WSDL element not specified in the binding");
    }

    /*
     * Parses the service name and port name from the WSDL element.
     */
    private WsdlElement parseWsdlElement(String wsdlElement, URL wsdlLocation) throws GenerationException {
        
        String[] token = wsdlElement.split("#");
        String namespaceUri = token[0];
        
        if (token[1].startsWith("wsdl.port")) {
            throw new GenerationException("Only WSDL 1.1 ports are currently supported");
        }
        token = token[1].substring(token[1].indexOf('('), token[1].indexOf(')')).split("/");

        // TODO verify against the WSDL is WSDL location is specified
        return new WsdlElement(new QName(namespaceUri, token[0]), new QName(namespaceUri, token[1]));
        
    }

    /*
     * Synthesizes the service name and port name from the service contract.
     */
    private WsdlElement synthesizeFromContract(ServiceContract<?> serviceContract) throws GenerationException {
        
        if (serviceContract instanceof JavaServiceContract) {
            
            JavaServiceContract javaServiceContract = (JavaServiceContract) serviceContract;
            String qualifedName = javaServiceContract.getInterfaceName();
            String packageName = qualifedName.substring(0, qualifedName.lastIndexOf('.'));
            String unqualifiedName = qualifedName.substring(qualifedName.lastIndexOf('.'));
            
            return new WsdlElement(new QName(packageName, unqualifiedName + "Service"), new QName(packageName, unqualifiedName + "Port"));
            
        } else {
            // TODO Support interface.wsdl
            throw new GenerationException("Service contract not supported : " + serviceContract.getClass());
        }
        
    }

}
