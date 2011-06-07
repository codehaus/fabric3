/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime.federation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.binding.zeromq.runtime.SocketAddress;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.JoinDomain;
import org.fabric3.spi.federation.MessageException;
import org.fabric3.spi.federation.MessageReceiver;
import org.fabric3.spi.federation.TopologyListener;
import org.fabric3.spi.federation.ZoneChannelException;
import org.fabric3.spi.federation.ZoneTopologyService;

/**
 * An implementation which propagates socket information throughout a distributed domain using the runtime federation API.
 *
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
@Service(interfaces = {AddressCache.class, TopologyListener.class, Fabric3EventListener.class})
public class FederatedAddressCache extends LocalAddressCache implements TopologyListener, MessageReceiver, Fabric3EventListener<JoinDomain> {
    private static final String ZEROMQ_CHANNEL = "ZeroMQChannel";
    private ZoneTopologyService topologyService;
    private HostInfo info;

    public FederatedAddressCache(@Reference ZoneTopologyService topologyService, @Reference HostInfo info) {
        this.topologyService = topologyService;
        this.info = info;
    }

    @Init
    public void init() throws MessageException {
        topologyService.openChannel(ZEROMQ_CHANNEL, null, this);
        topologyService.register(this);
    }

    @Destroy
    public void destroy() throws ZoneChannelException {
        topologyService.closeChannel(ZEROMQ_CHANNEL);
        topologyService.deregister(this);
    }

    @Override
    public void publish(AddressEvent event) {
        if (event instanceof AddressAnnouncement) {
            try {
                topologyService.sendAsynchronous(ZEROMQ_CHANNEL, event);
                super.publish(event);
            } catch (MessageException e) {
                // TODO monitor
            }
        }
    }

    public void onMessage(Object object) {
        if (object instanceof AddressAnnouncement) {
            super.publish((AddressAnnouncement) object);
        } else if (object instanceof AddressRequest) {
            AddressRequest request = (AddressRequest) object;
            AddressUpdate update = new AddressUpdate();
            for (Map.Entry<String, List<SocketAddress>> entry : addresses.entrySet()) {
                for (SocketAddress address : entry.getValue()) {
                    if (info.getRuntimeName().equals(address.getRuntimeName())) {
                        AddressAnnouncement announcement = new AddressAnnouncement(entry.getKey(), AddressAnnouncement.Type.ACTIVATED, address);
                        update.addAnnouncement(announcement);
                    }
                }
            }
            if (!update.getAnnouncements().isEmpty()) {
                try {
                    topologyService.sendAsynchronous(request.getRuntimeName(), ZEROMQ_CHANNEL, update);
                } catch (MessageException e) {
                    // TODO monitor
                }
            }
        }
    }

    public void onEvent(JoinDomain event) {
        try {
            AddressRequest request = new AddressRequest(info.getRuntimeName());
            topologyService.sendAsynchronous(ZEROMQ_CHANNEL, request);
        } catch (MessageException e) {
            // TODO monitor
        }
    }

    public void onLeave(String name) {
        for (Map.Entry<String, List<SocketAddress>> entry : addresses.entrySet()) {
            List<SocketAddress> toDelete = new ArrayList<SocketAddress>();
            List<SocketAddress> list = entry.getValue();
            for (SocketAddress address : list) {
                if (name.equals(address.getRuntimeName())) {
                    toDelete.add(address);
                }
            }
            for (SocketAddress address : toDelete) {
                list.remove(address);
            }
            if (list.isEmpty()) {
                addresses.remove(entry.getKey());
            }
        }
    }

    public void onJoin(String name) {
        // no-op
    }

    public void onLeaderElected(String name) {
        // no-op
    }

}
