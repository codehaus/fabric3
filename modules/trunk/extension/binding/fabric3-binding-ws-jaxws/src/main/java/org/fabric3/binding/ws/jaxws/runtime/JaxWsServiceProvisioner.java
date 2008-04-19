package org.fabric3.binding.ws.jaxws.runtime;

import java.lang.reflect.InvocationHandler;
import java.net.URI;

import org.fabric3.binding.ws.jaxws.provision.JaxWsWireSourceDefinition;
import org.fabric3.spi.builder.component.WireAttachException;

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
    
}
