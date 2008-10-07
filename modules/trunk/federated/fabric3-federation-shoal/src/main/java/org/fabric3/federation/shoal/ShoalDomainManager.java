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
package org.fabric3.federation.shoal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import com.sun.enterprise.ee.cms.core.GMSException;
import com.sun.enterprise.ee.cms.core.Signal;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import static org.fabric3.federation.shoal.FederationConstants.DOMAIN_MANAGER;
import static org.fabric3.federation.shoal.FederationConstants.ZONE_MANAGER;
import org.fabric3.spi.topology.DomainManager;
import org.fabric3.spi.topology.MessageException;
import org.fabric3.spi.topology.Zone;

/**
 * @version $Revision$ $Date$
 */
public class ShoalDomainManager implements DomainManager, FederationCallback {
    private FederationService federationService;

    /**
     * Constructor
     *
     * @param federationService the service responsible for managing domain runtime communications
     */
    public ShoalDomainManager(@Reference FederationService federationService) {
        this.federationService = federationService;
    }

    @Init
    public void init() {
        federationService.registerZoneCallback(DOMAIN_MANAGER, this);
    }

    public List<Zone> getZones() {
        List<String> members = federationService.getDomainGMS().getGroupHandle().getCurrentCoreMembers();
        List<Zone> zones = new ArrayList<Zone>(members.size());
        for (String member : members) {
            zones.add(new Zone(member));
        }
        return zones;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends Serializable> T getTransportMetaData(String zone, Class<T> type, QName transport) {
        Map<Serializable, Serializable> details = federationService.getDomainGMS().getMemberDetails(zone);
        if (details == null) {
            return null;
        }
        Map<QName, T> transportMetadata = (Map<QName, T>) details.get(FederationConstants.ZONE_TRANSPORT_INFO);
        if (transportMetadata == null) {
            throw new AssertionError("Transport metadata not found");
        }
        return transportMetadata.get(transport);
    }

    public void sendMessage(String zoneName, byte[] message) throws MessageException {
        try {
            federationService.getDomainGMS().getGroupHandle().sendMessage(zoneName, ZONE_MANAGER, message);
        } catch (GMSException e) {
            throw new MessageException(e);
        }
    }


    public void afterJoin() {
        // no op
    }

    public void onLeave() {
        // no op
    }

    public void onSignal(Signal signal) {
        // no op
    }
}

