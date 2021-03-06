/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.binding.ws.axis2.provision.jaxb;

import java.net.URI;
import java.util.Set;

import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;

/**
 * @version $Revision$ $Date$
 */
public class JaxbInterceptorDefinition extends PhysicalInterceptorDefinition {
    
    private final URI classLoaderId;
    private final Set<String> classNames;
    private final Set<String> faultNames;
    private final boolean service;
    
    public JaxbInterceptorDefinition(URI classLoaderId, Set<String> classNames, Set<String> faultNames, boolean service) {
        this.classLoaderId = classLoaderId;
        this.classNames = classNames;
        this.faultNames = faultNames;
        this.service = service;
    }

    public URI getClassLoaderId() {
        return classLoaderId;
    }

    public Set<String> getClassNames() {
        return classNames;
    }

    public Set<String> getFaultNames() {
        return faultNames;
    }

    public boolean isService() {
        return service;
    }

}
