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
import java.net.ServerSocket;
import java.net.URI;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.store.amq.AMQPersistenceAdapter;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.activemq.factory.InvalidConfigurationException;
import org.fabric3.host.runtime.HostInfo;

/**
 * Creates an embedded ActiveMQ broker.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class BrokerEngine {
    private String brokerName = "DefaultBroker";
    private BrokerService broker;
    private File tempDir;
    private int selectedPort = 61616;
    private String hostAddress = "localhost";
    private int maxPort = 71717;
    private int minPort = 61616;
    private File dataDir;
    private BrokerConfiguration brokerConfiguration;

    public BrokerEngine(@Reference HostInfo info) {
        tempDir = new File(info.getTempDir(), "activemq");
        // sets the directory where persistent messages are written
        File baseDataDir = info.getDataDir();
        dataDir = new File(baseDataDir, "activemq.data");
    }

    @Property(required = false)
    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    @Property(required = false)
    public void setBrokerConfig(XMLStreamReader reader) throws InvalidConfigurationException, XMLStreamException {
        BrokerParser parser = new BrokerParser();
        brokerConfiguration = parser.parse(reader);
    }

    @Init
    public void init() throws Exception {
        broker = new BrokerService();
        // TODO enable JMX via the F3 JMX agent
        // JMX must be turned off prior to configuring connections to avoid conflicts with the F3 JMX agent.
        broker.setUseJmx(false);
        broker.setTmpDataDirectory(tempDir);
        broker.setDataDirectory(dataDir.toString());
        if (brokerConfiguration == null) {
            // default configuration
            broker.setBrokerName(brokerName);
            boolean loop = true;
            TransportConnector connector = null;
            while (loop) {
                try {
                    connector = broker.addConnector("tcp://" + hostAddress + ":" + selectedPort);
                    loop = false;
                } catch (IOException e) {
                    selectPort();
                }
            }
            connector.setDiscoveryUri(URI.create("multicast://default"));
            broker.addNetworkConnector("multicast://default");
        } else {
            broker.setBrokerName(brokerConfiguration.getName());
            PersistenceAdapterConfig persistenceConfig = brokerConfiguration.getPersistenceAdapter();
            if (persistenceConfig != null) {
                if (PersistenceAdapterConfig.Type.AMQ == persistenceConfig.getType()) {
                    AMQPersistenceAdapter adapter = new AMQPersistenceAdapter();
                    adapter.setIndexBinSize(persistenceConfig.getIndexBinSize());
                    adapter.setCheckpointInterval(persistenceConfig.getCheckpointInterval());
                    adapter.setCleanupInterval(persistenceConfig.getCleanupInterval());
                    adapter.setIndexKeySize(persistenceConfig.getIndexKeySize());
                    adapter.setIndexPageSize(persistenceConfig.getIndexPageSize());
                    adapter.setSyncOnWrite(persistenceConfig.isSyncOnWrite());
                    adapter.setDisableLocking(persistenceConfig.isDisableLocking());
                    broker.setPersistenceAdapter(adapter);
                }
            }
        }
        broker.start();
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
            } catch (IOException e) {
                selectedPort++;
            }
        }
        selectedPort = -1;
        throw new IOException(
                "Unable to find an available port. Check to ensure the system configuration specifies an open port or port range.");
    }


}
