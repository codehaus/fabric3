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

import java.net.URI;
import java.net.URL;

import javax.xml.namespace.QName;

import org.fabric3.spi.generator.GenerationException;

/**
 * Resolves the address on which service is provisioned or reference is invoked.
 *
 */
public interface AddressResolver {
    
    /**
     * Resolves the address on which the service is provisioned.
     * 
     * @param targetUri Target URI specified on the service binding.
     * @param portName Qualified name of the port.
     * @param wsdlLocation URL to the WSDL document.
     * @return URI on which the service is provisioned.
     * 
     * @throws GenerationException If unable to resolve the address.
     */
    URI resolveServiceAddress(URI targetUri, QName portName, URL wsdlLocation) throws GenerationException;
    
    /**
     * Resolves the address on which the service is provisioned.
     * 
     * @param targetUri Target URI specified on the reference binding.
     * @param portName Qualified name of the port.
     * @param wsdlLocation URL to the WSDL document.
     * @return List of URLs on which the service can be invoked..
     * 
     * @throws GenerationException If unable to resolve the address.
     */
    URL[] resolveReferenceAddress(URI targetUri, QName portName, URL wsdlLocation) throws GenerationException;

}
