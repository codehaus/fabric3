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
package org.fabric3.messaging.jxta;

import net.jxta.peer.PeerID;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.jxta.JxtaService;
import org.fabric3.spi.services.VoidService;
import org.fabric3.spi.services.runtime.RuntimeInfoService;

/**
 * Registers the message destination for the current runtime with the RuntimeInfoService.
 *
 * @version $Revsion$ $Date$
 */
@EagerInit
public class JxtaMessageDestinationRegisterer implements VoidService {
    private RuntimeInfoService runtimeInfoService;
    private JxtaService jxtaService;

    public JxtaMessageDestinationRegisterer(@Reference RuntimeInfoService runtimeInfoService,
                                            @Reference JxtaService jxtaService) {
        this.runtimeInfoService = runtimeInfoService;
        this.jxtaService = jxtaService;
    }

    @Init
    public void init() {
        PeerID peerID = jxtaService.getDomainGroup().getPeerID();
        runtimeInfoService.registerMessageDestination(peerID.toString());
    }

}
