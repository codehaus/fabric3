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
package org.fabric3.spi.services.discovery;

import java.util.List;

/**
 * A registry of DiscoveryServices. Used in runtimes that support more than one DiscoveryService.
 *
 * @version $Rev$ $Date$
 */
public interface DiscoveryServiceRegistry {

    /**
     * Registers a DiscoveryService
     *
     * @param service the service to register
     */
    void register(DiscoveryService service);

    /**
     * Un-registers a DiscoveryService
     *
     * @param service the service to unregister
     */
    void unRegister(DiscoveryService service);

    /**
     * Returns all registered DiscoveryServices.
     *
     * @return all registered DiscoveryServices
     */
    List<DiscoveryService> getServices();

}
