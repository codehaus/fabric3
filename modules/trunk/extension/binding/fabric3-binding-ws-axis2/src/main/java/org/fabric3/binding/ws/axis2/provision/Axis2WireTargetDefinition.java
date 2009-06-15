/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.binding.ws.axis2.provision;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.binding.ws.provision.WsdlElement;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * @version $Rev$ $Date$
 */
public class Axis2WireTargetDefinition extends PhysicalWireTargetDefinition implements Axis2PolicyAware {


    private static final long serialVersionUID = 157147784561060006L;

    private String referenceInterface;
    private Map<String, Set<AxisPolicy>> policies = new HashMap<String, Set<AxisPolicy>>();
    private Map<String, Map<String, String>> operationInfo;
    private Map<String, String> config;
    private URI classloaderURI;
    private String wsdlLocation;
    private WsdlElement wsdlElement;

    /**
     * @return Reference interface for the wire target.
     */
    public String getReferenceInterface() {
        return referenceInterface;
    }

    /**
     * @param referenceInterface Reference interface for the wire target.
     */
    public void setReferenceInterface(String referenceInterface) {
        this.referenceInterface = referenceInterface;
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
    public Set<AxisPolicy> getPolicies(String operation) {
        return policies.get(operation);
    }

    public Map<String, Map<String, String>> getOperationInfo() {
        return operationInfo;
    }

    public void addOperationInfo(String operation, Map<String, String> operationInfo) {
        if (this.operationInfo == null) {
            this.operationInfo = new HashMap<String, Map<String, String>>();
        }
        this.operationInfo.put(operation, operationInfo);
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    /**
     * @param policy Policy definitions.
     */
    public void addPolicy(String operation, AxisPolicy policy) {

        if (!this.policies.containsKey(operation)) {
            this.policies.put(operation, new HashSet<AxisPolicy>());
        }
        this.policies.get(operation).add(policy);
    }

    public String getWsdlLocation() {
        return wsdlLocation;
    }

    public void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }

    public WsdlElement getWsdlElement() {
        return wsdlElement;
    }

    public void setWsdlElement(WsdlElement wsdlElement) {
        this.wsdlElement = wsdlElement;
    }

}
