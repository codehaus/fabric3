package org.fabric3.jxta.impl;

import java.io.File;
import java.io.IOException;

import javax.security.cert.CertificateException;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.impl.id.UUID.UUID;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.NetPeerGroupFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.protocol.ModuleImplAdvertisement;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.jxta.JxtaService;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Property;

/**
 * Default implementation of the JXTA service.
 *
 * @version $Revsion$ $Date$
 *
 */
@EagerInit
public class JxtaServiceImpl implements JxtaService {

    /**
     * Well known peer group id.
     *
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
    private int tcpPort;

    /**
     * @see org.fabric3.jxta.JxtaService#getDomainGroup()
     */
    public PeerGroup getDomainGroup() {
        return domainGroup;
    }

    /**
     * Injects the host info.
     * @param hostInfo Information on the host.
     */
    @Reference
    public void setHostInfo(HostInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

    /**
     * Injects the JXTA network configurator. This is required to be configure with
     * the principal and credentials to stop the JXTA configuration window being
     * started interactively. The configurator can also be pre-injected with the
     * protocol parameters like TCP listen port etc.
     *
     * @param networkConfigurator JXTA network configurator.
     */
    @Reference
    public void setNetworkConfigurator(NetworkConfigurator networkConfigurator) {
        this.networkConfigurator = networkConfigurator;
    }

    @Property
    public void setTcpPort(String tcpPort) {
        this.tcpPort = Integer.parseInt(tcpPort);
    }

    /**
     * Joins the domain and creates the discovery and resolver services.
     *
     */
    @Init
    public void start() {

        assert hostInfo != null;
        assert networkConfigurator != null;

        configure();
        createAndJoinDomainGroup();

    }

    /**
     * Configures the platform.
     */
    private void configure() {

        try {

            String runtimeId = hostInfo.getRuntimeId();

            networkConfigurator.setName(runtimeId);
            networkConfigurator.setHome(new File(runtimeId));
            networkConfigurator.setTcpPort(tcpPort);
            // FIXME Once property support is available
            networkConfigurator.setPassword("test-password");
            networkConfigurator.setPrincipal("test-principal");

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
     *
     * @throws Exception In case of unexpected JXTA exceptions.
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
    @SuppressWarnings("serial")
    private static class Fabric3PeerGroupID extends net.jxta.impl.id.CBID.PeerGroupID {
        public Fabric3PeerGroupID(UUID uuid) {
            super(uuid);
        }
    }

}
