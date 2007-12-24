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
package org.fabric3.fabric.services.messaging;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.services.messaging.MessagingService;
import org.fabric3.spi.services.messaging.MessagingServiceRegistry;

/**
 * Default MessagingServiceRegistry implementation.
 *
 * @version $Rev$ $Date$
 */
public class MessagingServiceRegistryImpl implements MessagingServiceRegistry {
    private Map<String, MessagingService> services = new HashMap<String, MessagingService>();

    public void register(MessagingService service) {
        if (!services.isEmpty()) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
        services.put("scheme", service);
    }

    public void unRegister(MessagingService service) {
        services.remove("scheme");
    }

    public MessagingService getServiceForScheme(String scheme) {
        return services.get("scheme");
    }
}
