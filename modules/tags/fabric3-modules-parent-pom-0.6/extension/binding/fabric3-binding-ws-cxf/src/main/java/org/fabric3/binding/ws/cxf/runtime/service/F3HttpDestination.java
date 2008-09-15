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

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

/**
 * A simple HTTP-based Destination.
 *
 * @version $Rev$ $Date$
 */
public class F3HttpDestination extends AbstractHTTPDestination {

    public F3HttpDestination(Bus bus, ConduitInitiator initiator, EndpointInfo ei, boolean defaultPort)
            throws IOException {
        super(bus, initiator, ei, defaultPort);
    }

    protected Logger getLogger() {
        return null;
    }
}
