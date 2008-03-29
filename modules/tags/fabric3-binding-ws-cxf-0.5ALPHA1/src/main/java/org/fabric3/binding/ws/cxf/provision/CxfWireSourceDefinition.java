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
package org.fabric3.binding.ws.cxf.provision;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 * Physical wire source definition for Hessian binding.
 *
 * @version $Revision: 1589 $ $Date: 2007-10-25 23:13:37 +0100 (Thu, 25 Oct 2007) $
 */
public class CxfWireSourceDefinition extends PhysicalWireSourceDefinition {

    /**
     * Interface for the service.
     */
    private String serviceInterface;

    /**
     * The classloader for the service
     */
    private URI classloaderURI;

    /**
     * @return Service interface.
     */
    public String getServiceInterface() {
        return serviceInterface;
    }

    /**
     * @param serviceInterface Service interface.
     */
    public void setServiceInterface(String serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public URI getClassloaderURI() {
        return classloaderURI;
    }

    public void setClassloaderURI(URI classloaderURI) {
        this.classloaderURI = classloaderURI;
    }
}
