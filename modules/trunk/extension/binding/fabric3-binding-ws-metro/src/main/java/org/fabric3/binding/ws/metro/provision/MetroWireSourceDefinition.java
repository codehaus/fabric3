/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
package org.fabric3.binding.ws.metro.provision;

import java.net.URI;
import java.net.URL;

import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 * Wire source definition for Metro binding.
 *
 */
public class MetroWireSourceDefinition extends PhysicalWireSourceDefinition {

    private WsdlElement wsdlElement;
    private URL wsdlUrl;
    private URI servicePath;

    /**
     * Initialises information required for provisioning the service.
     * 
     * @param wsdlElement WSDL element that encasulates the qualified WSDL 1.1 service and port names.
     * @param wsdlUrl Optional URL to the WSDL location.
     * @param servicePath Relative path on which the service is provisioned.
     */
    public MetroWireSourceDefinition(WsdlElement wsdlElement, URL wsdlUrl, URI servicePath) {
        this.wsdlElement = wsdlElement;
        this.wsdlUrl = wsdlUrl;
        this.servicePath = servicePath;
    }

    /**
     * Gets the WSDL element that encasulates the qualified WSDL 1.1 service and port names.
     * 
     * @return WSDL element that encasulates the qualified WSDL 1.1 service and port names.
     */
    public WsdlElement getWsdlElement() {
        return wsdlElement;
    }

    /**
     * Gets an optional URL to the WSDL document.
     * 
     * @return Optional URL to the WSDL document.
     */
    public URL getWsdlUrl() {
        return wsdlUrl;
    }

    /**
     * Gets the relative path on which the service is provisioned.
     * 
     * @return Relative path on which the service is provisioned.
     */
    public URI getServicePath() {
        return servicePath;
    }

}
