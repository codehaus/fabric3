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

package org.fabric3.binding.ws.model.logical;

import java.net.URI;

import org.fabric3.scdl.BindingDefinition;

/**
 * Logical binding definition for web services.
 * 
 * @version $Revision$ $Date$
 */
public class WsBindingDefinition extends BindingDefinition {
    
    private final String implementation;
    private final String wsdlLocation;
    private final String wsdlElement;

    public WsBindingDefinition(URI targetUri, String implementation,
                               String wsdlLocation, String wsdlElement) {
        super(targetUri, WsBindingLoader.BINDING_QNAME);
        this.implementation = implementation;
        this.wsdlElement = wsdlElement;
        this.wsdlLocation = wsdlLocation;
    }
    
    public String getImplementation() {
        return implementation;
    }


    public String getWsdlElement() {
        return wsdlElement;
    }

    public String getWsdlLocation() {
        return wsdlLocation;
    }

}
