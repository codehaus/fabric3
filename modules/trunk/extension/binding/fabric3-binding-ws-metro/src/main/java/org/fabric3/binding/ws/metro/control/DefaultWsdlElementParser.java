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

import javax.xml.namespace.QName;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;

import org.fabric3.binding.ws.provision.WsdlElement;
import org.fabric3.scdl.JavaServiceContract;
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
     * @param wsdlModel Model object representing the WSDL information.
     * @param serviceContract Service contract for the WSDL.
     * @return Parsed WSDL element.
     * @throws GenerationException If unable to parse the WSDL element.
     */
    public WsdlElement parseWsdlElement(String wsdlElement, WSDLModel wsdlModel, ServiceContract<?> serviceContract) throws GenerationException {
        
        // No wsdl element, just location, parse the WSDL location
        if (wsdlElement == null && wsdlModel != null) {
            return parseWsdl(wsdlModel);
        }
        
        // Wsdl element present, so parse it
        if (wsdlElement != null) {
            return parseWsdlElement(wsdlElement, wsdlModel);
        }
        
        // No wsdl element or location, synthesize the names
        return synthesizeFromContract(serviceContract);
        
    }

    /*
     * Parses the service name and port name from the WSDL.
     */
    private WsdlElement parseWsdl(WSDLModel wsdlModel) throws GenerationException {
        
        WSDLService wsdlService = wsdlModel.getServices().values().iterator().next();
        if (wsdlService == null) {
            throw new GenerationException("WSDL doesn't contain any service");
        }
        WSDLPort wsdlPort = wsdlService.getFirstPort();
        if (wsdlPort == null) {
            throw new GenerationException("WSDL doesn't contain any port");
        }
        
        return new WsdlElement(wsdlService.getName(), wsdlPort.getName());
        
    }

    /*
     * Parses the service name and port name from the WSDL element.
     */
    private WsdlElement parseWsdlElement(String wsdlElement, WSDLModel wsdlModel) throws GenerationException {
        
        String[] token = wsdlElement.split("#");
        String namespaceUri = token[0];
        
        if (!token[1].startsWith("wsdl.port")) {
            throw new GenerationException("Only WSDL 1.1 ports are currently supported");
        }
        token = token[1].substring(token[1].indexOf('(') + 1, token[1].indexOf(')')).split("/");

        QName serviceName = new QName(namespaceUri, token[0]);
        QName portName = new QName(namespaceUri, token[1]);
        
        if (wsdlModel != null) {
            WSDLService wsdlService = wsdlModel.getService(serviceName);
            if (wsdlService == null) {
                throw new GenerationException("Service " + serviceName + " not found in WSDL");
            }
            if (wsdlService.get(portName) == null) {
                throw new GenerationException("Port " + portName + " not found in WSDL");
            }
        }
        
        return new WsdlElement(serviceName, portName);
        
    }

    /*
     * Synthesizes the service name and port name from the service contract.
     */
    private WsdlElement synthesizeFromContract(ServiceContract<?> serviceContract) throws GenerationException {
        
        if (serviceContract instanceof JavaServiceContract) {
            
            JavaServiceContract javaServiceContract = (JavaServiceContract) serviceContract;
            String qualifedName = javaServiceContract.getInterfaceClass();
            String packageName = qualifedName.substring(0, qualifedName.lastIndexOf('.'));
            String unqualifiedName = qualifedName.substring(qualifedName.lastIndexOf('.') + 1);
            
            return new WsdlElement(new QName(packageName, unqualifiedName + "Service"), new QName(packageName, unqualifiedName + "Port"));
            
        } else {
            // TODO Support interface.wsdl
            throw new GenerationException("Service contract not supported : " + serviceContract.getClass());
        }
        
    }

}
