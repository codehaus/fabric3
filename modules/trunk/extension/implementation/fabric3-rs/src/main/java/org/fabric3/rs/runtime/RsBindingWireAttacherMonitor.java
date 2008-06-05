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
package org.fabric3.rs.runtime;

import java.net.URI;

import org.fabric3.api.annotation.logging.Info;

/**
 * @version $Rev$ $Date$
 */
public interface RsBindingWireAttacherMonitor {

    /**
     * Callback when a service has been provisioned as a REST endpoint
     *
     * @param address the endpoint address
     */
    @Info
    void provisionedEndpoint( String className, String type, URI address);

    /**
     * Callback when a service endpoint has been de-provisioned
     *
     * @param address the endpoint address
     */
    @Info
    void removedEndpoint( String className, String type, URI address);

    /**
     * Callback indicating the extension has been initialized.
     */
    @Info
    void extensionStarted();

    /**
     * Callback indicating the extension has been stopped.
     */
    @Info
    void extensionStopped();
}
