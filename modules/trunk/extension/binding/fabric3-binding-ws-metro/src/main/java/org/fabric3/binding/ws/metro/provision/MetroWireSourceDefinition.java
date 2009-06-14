/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
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
package org.fabric3.binding.ws.metro.provision;

import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.fabric3.binding.ws.provision.WsdlElement;
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
     * @param requestedIntents Intents requested by the binding.
     */
    public MetroWireSourceDefinition(WsdlElement wsdlElement, URL wsdlUrl, URI servicePath, String interfaze, List<QName> requestedIntents) {
        this.wsdlElement = wsdlElement;
        this.wsdlUrl = wsdlUrl;
        this.servicePath = servicePath;
        this.interfaze = interfaze;
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
