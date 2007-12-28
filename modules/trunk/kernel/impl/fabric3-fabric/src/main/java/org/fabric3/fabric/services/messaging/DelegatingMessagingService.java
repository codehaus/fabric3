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

import java.net.URI;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.services.messaging.MessagingException;
import org.fabric3.spi.services.messaging.MessagingService;
import org.fabric3.spi.services.messaging.MessagingServiceRegistry;

/**
 * MessagingService implementation that deleagtes to other MessagingServices. Messages will be dispatched based on the
 * runtime id scheme.
 *
 * @version $Rev$ $Date$
 */
@EagerInit

public class DelegatingMessagingService implements MessagingService {
    private MessagingServiceRegistry registry;

    public DelegatingMessagingService(@Reference MessagingServiceRegistry registry) {
        this.registry = registry;
    }

    public String getScheme() {
        return null;
    }

    public void sendMessage(URI runtimeId, XMLStreamReader content) throws MessagingException {
        // FIXME selectively dispatch message based on a runtime scheme
        String scheme = null;
        MessagingService service = registry.getServiceForScheme(scheme);
        if (service == null) {
            throw new UnknownMessagingSchemeException("No messaging service for scheme [" + scheme + "]", scheme);
        }
        service.sendMessage(runtimeId, content);
    }

}
