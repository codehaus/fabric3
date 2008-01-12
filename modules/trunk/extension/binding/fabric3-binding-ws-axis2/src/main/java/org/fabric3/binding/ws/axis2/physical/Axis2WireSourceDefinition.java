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
package org.fabric3.binding.ws.axis2.physical;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class Axis2WireSourceDefinition extends PhysicalWireSourceDefinition implements Axis2PolicyAware {

    private String serviceInterface;
    private Map<String, Set<Element>> policyDefinitions = new HashMap<String, Set<Element>>();
    private URI classloaderURI;

    /**
     * @return Service interface for the wire source.
     */
    public String getServiceInterface() {
        return serviceInterface;
    }

    /**
     * @param serviceInterface Service interface for the wire source.
     */
    public void setServiceInterface(String serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    /**
     * @return Classloader URI.
     */
    public URI getClassloaderURI() {
        return classloaderURI;
    }

    /**
     * @param classloaderURI Classloader URI.
     */
    public void setClassloaderURI(URI classloaderURI) {
        this.classloaderURI = classloaderURI;
    }

    /**
     * @return Policy definitions.
     */
    public Set<Element> getPolicyDefinitions(String operation) {
        return policyDefinitions.get(operation);
    }

    /**
     * @param policyDefinitions Policy definitions.
     */
    public void addPolicyDefinition(String operation, Element policyDefinitions) {
        
        if (!this.policyDefinitions.containsKey(operation)) {
            this.policyDefinitions.put(operation, new HashSet<Element>());
        }
        this.policyDefinitions.get(operation).add(policyDefinitions);
    }

}
