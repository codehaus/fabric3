/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.spring;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.scdl.ServiceDefinition;

/**
 * A component type specialization for Spring implementations
 *
 * @version $$Rev$$ $$Date$$
 */
public class SpringComponentType extends AbstractComponentType<ServiceDefinition, ReferenceDefinition, Property, ResourceDefinition> {
    private static final long serialVersionUID = -2086494614136103280L;

    // override super class's object since we need to change introspected
    // serviceName to declared serviceName, which is equal to beanId
    private final Map<String, ServiceDefinition> services = new HashMap<String, ServiceDefinition>();

    /**
     * Returns a live Map of the services provided by the implementation.
     *
     * @return a live Map of the services provided by the implementation
     */
    @Override
    public Map<String, ServiceDefinition> getServices() {
        return services;
    }

    /**
     * Add a service to those provided by the implementation. Any existing service with the same name is replaced.
     *
     * @param service a service provided by the implementation
     */
    public void add(String serviceName, ServiceDefinition service) {
        services.put(serviceName, service);
    }

}
