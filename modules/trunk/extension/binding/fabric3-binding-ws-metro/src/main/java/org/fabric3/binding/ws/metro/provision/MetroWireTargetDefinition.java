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
package org.fabric3.binding.ws.metro.provision;

import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Reference-side wire target definition for the Metro binding.
 *
 * @version $Rev$ $Date$
 */
public class MetroWireTargetDefinition extends PhysicalWireTargetDefinition {
    private static final long serialVersionUID = 5758003268658918242L;

    private ReferenceEndpointDefinition endpointDefinition;
    private URL wsdlLocation;
    private String interfaze;
    private List<QName> requestedIntents;
    private List<PolicyExpressionMapping> mappings;
    private SecurityConfiguration configuration;

    /**
     * Constructor.
     *
     * @param endpointDefinition endpoint information
     * @param wsdlLocation       optional URL to the WSDL location
     * @param interfaze          the service contract name
     * @param requestedIntents   intents requested by the binding
     * @param mappings           mappings of policy expressions to the operations they are attached to. Used to construct dynamic client WSDL
     * @param configuration      the security configuration
     */
    public MetroWireTargetDefinition(ReferenceEndpointDefinition endpointDefinition,
                                     URL wsdlLocation,
                                     String interfaze,
                                     List<QName> requestedIntents,
                                     List<PolicyExpressionMapping> mappings,
                                     SecurityConfiguration configuration) {
        this.endpointDefinition = endpointDefinition;
        this.wsdlLocation = wsdlLocation;
        this.interfaze = interfaze;
        this.requestedIntents = requestedIntents;
        this.mappings = mappings;
        this.configuration = configuration;
    }

    /**
     * Returns the endpoint information.
     *
     * @return the endpoint information
     */
    public ReferenceEndpointDefinition getEndpointDefinition() {
        return endpointDefinition;
    }

    /**
     * Returns an optional URL to the WSDL document.
     *
     * @return optional URL to the WSDL document
     */
    public URL getWsdlLocation() {
        return wsdlLocation;
    }

    /**
     * Returns the service contract name.
     *
     * @return the service contract name
     */
    public String getInterface() {
        return interfaze;
    }

    /**
     * Returns the intents requested by the binding.
     *
     * @return intents requested by the binding
     */
    public List<QName> getRequestedIntents() {
        return requestedIntents;
    }

    /**
     * Returns the mappings from policy expression to operations.
     *
     * @return the mappings from policy expression to operations
     */
    public List<PolicyExpressionMapping> getMappings() {
        return mappings;
    }

    /**
     * Returns the security configuration.
     *
     * @return the security configuration
     */
    public SecurityConfiguration getConfiguration() {
        return configuration;
    }
}
