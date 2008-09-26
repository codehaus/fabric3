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
package org.fabric3.binding.ws.jaxws.runtime;

import java.lang.reflect.InvocationHandler;
import java.net.URI;

import org.fabric3.binding.ws.jaxws.provision.JaxWsWireSourceDefinition;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.builder.WiringException;

public interface JaxWsServiceProvisioner {

    /**
     * Provision a JAX-WS service for the given service interface by generating
     * necessary bytecode with JAX-WS web service annotations
     * @param clazz Service interface
     * @param handler Pass an instance of InvocationHandler that implements the service
     *        interface
     * @param source source definition of the web service
     * @param targetUri
     * @throws WireAttachException
     */
    void provision(Class<?> clazz, InvocationHandler handler, JaxWsWireSourceDefinition source,
                   URI targetUri) throws WireAttachException;


    /**
     * Unprovision JAX_WS service previously generated. Failing to stop the service should result
     * in WiringException
     * @param source source definition of the web service
     * @param targetUri
     * @throws WiringException
     */
    void unprovision(JaxWsWireSourceDefinition source,
                   URI targetUri) throws WiringException;
    
}
