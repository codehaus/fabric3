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
package org.fabric3.binding.ws.cxf.runtime.service;

import java.net.URI;

import org.fabric3.spi.wire.Wire;

/**
 * Manages the CXF subsystem.
 *
 * @version $Rev$ $Date$
 */
public interface CXFService {

    /**
     * Provisions a a wire's target service as a Web Service endpoint at the given URI
     *
     * @param uri       the endpoint uri
     * @param interfaze the service interface
     * @param wire      the wire to the target service being provisioned
     * @throws EndpointProvisionException if an error is encountered provisioning the endpoint
     */
    void provisionEndpoint(URI uri, Class<?> interfaze, Wire wire) throws EndpointProvisionException;

    /**
     * Binds a wire from a reference to a target web services endpoint
     *
     * @param uri       the endpoint uri
     * @param interfaze the service interface for the reference
     * @param wire      the wire to the target service being provisioned
     * @throws TargetBindException if an error is encountered binding the wire
     */
    void bindToTarget(URI uri, Class<?> interfaze, Wire wire) throws TargetBindException;

}
