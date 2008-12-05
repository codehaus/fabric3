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

import org.fabric3.binding.ws.provision.WsdlElement;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.generator.GenerationException;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;

/**
 * Interface for parsing the WSDL element.
 *
 */
public interface WsdlElementParser {
    
    /**
     * Parses the WSDL element.
     * 
     * @param wsdlElement String representation of the WSDL element.
     * @param wsdlModel Model object containing the WSDL information.
     * @param serviceContract Service contract for the WSDL.
     * @return Parsed WSDL element.
     * @throws GenerationException If unable to parse the WSDL element.
     */
    WsdlElement parseWsdlElement(String wsdlElement, WSDLModel wsdlModel, ServiceContract<?> serviceContract) throws GenerationException;

}
