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
package org.fabric3.fabric.services.messaging;

import java.net.URI;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.services.messaging.MessagingException;
import org.fabric3.spi.services.messaging.MessagingService;
import org.fabric3.spi.services.messaging.MessagingServiceRegistry;

/**
 * MessagingService implementation that deleagtes to other MessagingServices. Messages will be dispatched based on the runtime id scheme.
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
        String scheme = runtimeId.getScheme();
        MessagingService service = registry.getServiceForScheme(scheme);
        if (service == null) {
            throw new UnknownMessagingSchemeException("No messaging service for scheme: " + scheme, scheme);
        }
        service.sendMessage(runtimeId, content);
    }

}
