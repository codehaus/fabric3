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

import org.fabric3.spi.builder.WiringException;

/**
 * Denotes a general error provisioning a service endpoint.
 *
 * @version $Rev$ $Date$
 */
@SuppressWarnings("serial")
public class EndpointProvisionException extends WiringException {
    public EndpointProvisionException(Throwable cause) {
        super(cause);
    }

    public EndpointProvisionException(String message) {
        super(message);
    }

    public EndpointProvisionException(String message, URI sourceUri, URI targetUri) {
        super(message, sourceUri, targetUri);
    }

    public EndpointProvisionException(String message, URI sourceUri, URI targetUri, Throwable cause) {
        super(message, sourceUri, targetUri, cause);
    }

    public EndpointProvisionException(String message, String identifier, URI sourceUri, URI targetUri) {
        super(message, identifier, sourceUri, targetUri);
    }

    public EndpointProvisionException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }

    public EndpointProvisionException(String message, String identifier) {
        super(message, identifier);
    }
}
