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
package org.fabric3.binding.ws.metro.provision;

import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 * Wire source definition for Metro binding.
 *
 */
public class MetroWireSourceDefinition extends PhysicalWireSourceDefinition {
    private static final long serialVersionUID = -7874049193479847748L;

    private WsdlElement wsdlElement;
    private URL wsdlUrl;
    private URI servicePath;
    private String interfaze;
    private List<QName> requestedIntents;

    /**
     * Initialises information required for provisioning the service.
     * 
     * @param wsdlElement WSDL element that encasulates the qualified WSDL 1.1 service and port names.
     * @param wsdlUrl Optional URL to the WSDL location.
     * @param servicePath Relative path on which the service is provisioned.
     * @param interfaze Interface for the service contract.
     * @param classLoaderId ClassLoader Id.
     * @param requestedIntents Intents requested by the binding.
     */
    public MetroWireSourceDefinition(WsdlElement wsdlElement, URL wsdlUrl, URI servicePath, String interfaze, URI classLoaderId, List<QName> requestedIntents) {
        this.wsdlElement = wsdlElement;
        this.wsdlUrl = wsdlUrl;
        this.servicePath = servicePath;
        this.interfaze = interfaze;
        setClassLoaderId(classLoaderId);
        this.requestedIntents = requestedIntents;
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
    
    /**
     * Gets the interface for the service contract.
     * 
     * @return Interface for the service contract.
     */
    public String getInterfaze() {
        return interfaze;
    }

    /**
     * Gets the intents requested by the binding.
     * 
     * @return Intents requested by the binding.
     */
    public List<QName> getRequestedIntents() {
        return requestedIntents;
    }

}
