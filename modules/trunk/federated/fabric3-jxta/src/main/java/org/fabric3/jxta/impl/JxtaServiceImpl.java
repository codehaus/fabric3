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
package org.fabric3.jxta.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.security.cert.CertificateException;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.impl.id.UUID.UUID;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.NetPeerGroupFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.protocol.ModuleImplAdvertisement;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.jxta.JxtaService;
import org.fabric3.spi.services.runtime.RuntimeInfoService;

/**
 * Default implementation of the JXTA service.
 *
 * @version $Revsion$ $Date$
 */
@EagerInit
public class JxtaServiceImpl implements JxtaService {

    /**
     * Well known peer group id.
     * <p/>
     * TODO This may need to be generated from the domain name.
     */
    private static final Fabric3PeerGroupID PEER_GROUP_ID =
            new Fabric3PeerGroupID(new UUID("aea468a4-6450-47dc-a288-a7f1bbcc5927"));

    // Domain peer group
    private PeerGroup domainGroup;

    // Host info
    private HostInfo hostInfo;

    // Network configurator
    private NetworkConfigurator networkConfigurator;

    private RuntimeInfoService runtimeInfoService;

    public PeerGroup getDomainGroup() {
        return domainGroup;
    }

    /**
     * Injects the host info.
     *
     * @param hostInfo Information on the host.
     */
    @Reference
    public void setHostInfo(HostInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

    /**
     * Injects the JXTA network configurator. This is required to be configure with the principal and credentials to
     * stop the JXTA configuration window being started interactively. The configurator can also be pre-injected with
     * the protocol parameters like TCP listen port etc.
     *
     * @param networkConfigurator JXTA network configurator.
     */
    @Reference
    public void setNetworkConfigurator(NetworkConfigurator networkConfigurator) {
        this.networkConfigurator = networkConfigurator;
    }

    @Reference
    public void setRuntimeInfoService(RuntimeInfoService runtimeInfoService) {
        this.runtimeInfoService = runtimeInfoService;
    }

    /**
     * Joins the domain and creates the discovery and resolver services.
     */
    @Init
    public void start() {
        configure();
        createAndJoinDomainGroup();
    }

    /**
     * Configures the platform.
     */
    private void configure() {

        try {

            URI runtimeId = runtimeInfoService.getCurrentRuntimeId();
            // TODO temporary
            String id = runtimeId.toString();
            networkConfigurator.setName(id);
            networkConfigurator.setHome(new File(id));
            networkConfigurator.setMode(NetworkConfigurator.EDGE_NODE);

            if (networkConfigurator.exists()) {
                File pc = new File(networkConfigurator.getHome(), "PlatformConfig");
                networkConfigurator.load(pc.toURI());
                networkConfigurator.save();
            } else {
                networkConfigurator.save();
            }

        } catch (IOException ex) {
            throw new Fabric3JxtaException(ex);
        } catch (CertificateException ex) {
            throw new Fabric3JxtaException(ex);
        }

    }

    /**
     * Creates and joins the domain peer group.
     */
    private void createAndJoinDomainGroup() {

        try {

            String domain = hostInfo.getDomain().toString();

            PeerGroup netGroup = new NetPeerGroupFactory().getInterface();
            ModuleImplAdvertisement implAdv = netGroup.getAllPurposePeerGroupImplAdvertisement();
            domainGroup = netGroup.newGroup(PEER_GROUP_ID, implAdv, domain, "Fabric3 domain group");

            AuthenticationCredential authCred = new AuthenticationCredential(domainGroup, null, null);
            MembershipService membership = domainGroup.getMembershipService();
            Authenticator auth = membership.apply(authCred);

            if (auth.isReadyForJoin()) {
                membership.join(auth);
            } else {
                throw new Fabric3JxtaException("Unable to join domain group");
            }

        } catch (Exception ex) {
            throw new Fabric3JxtaException(ex);
        }

    }

    /*
     * Well known peer grroup.
     */
    private static class Fabric3PeerGroupID extends net.jxta.impl.id.CBID.PeerGroupID {
        private static final long serialVersionUID = 2529239017940982956L;

        public Fabric3PeerGroupID(UUID uuid) {
            super(uuid);
        }
    }

}
