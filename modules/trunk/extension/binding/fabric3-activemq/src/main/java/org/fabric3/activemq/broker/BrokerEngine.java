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
package org.fabric3.activemq.broker;

import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.URI;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.provider.ProviderRegistry;
import org.fabric3.host.runtime.HostInfo;

/**
 * Creates an embedded ActiveMQ broker that autodiscovers other brokers in the domain via multi-cast.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class BrokerEngine {
    private String brokerName = "DefaultBroker";
    private BrokerService broker;
    private File tempDir;
    private int selectedPort = 61616;
    private String port;
    private String hostAddress = "localhost";
    private ProviderRegistry registry;
    private int maxPort = 71717;
    private int minPort = 61616;
    private File dataDir;

    public BrokerEngine(@Reference HostInfo info, @Reference ProviderRegistry registry) {
        this.registry = registry;
        tempDir = new File(info.getTempDir(), "activemq");
        // sets the directory where persistent messages are written
        dataDir = new File(info.getBaseDir(), "activemq.data");
    }

    @Property(required = false)
    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

//    @Property(required = false)
//    public void setPort(String port) {
//        this.port = port;
//    }

    @Property(required = false)
    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    @Init
    public void init() throws Exception {
        selectPort();
        broker = new BrokerService();
        broker.setBrokerName(brokerName);
        broker.setTmpDataDirectory(tempDir);
        broker.setDataDirectory(dataDir.toString());
        // TODO enable JMX via the F3 JMX agent
        // JMX must be turned off prior to configuring connections to avoid conflicts with the F3 JMX agent.
        broker.setUseJmx(false);

        TransportConnector connector = broker.addConnector("tcp://" + hostAddress + ":" + selectedPort);
        connector.setDiscoveryUri(URI.create("multicast://default"));
        broker.addNetworkConnector("multicast://default");
        broker.start();

        // Make the ActiveMQ classes available to the JMS extension.
        // This allows the JMX extension to create connection factories using Class.forName().
        registry.registerProviderClassLoader(getClass().getClassLoader());
    }

    @Destroy
    public void destroy() throws Exception {
        if (broker != null) {
            broker.stop();
        }
    }

    private void selectPort() throws IOException {
        if (maxPort == -1) {
            selectedPort = minPort;
            return;
        }
        selectedPort = minPort;
        while (selectedPort <= maxPort) {
            try {
                ServerSocket socket = new ServerSocket(selectedPort);
                socket.close();
                return;
            } catch (BindException e) {
                selectedPort++;
            }
        }
        selectedPort = -1;
        throw new IOException(
                "Unable to find an available port. Check to ensure the system configuration specifies an open port or port range.");
    }


}
