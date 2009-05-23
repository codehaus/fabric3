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

import java.net.URI;
import java.util.List;

/**
 * Encapsulates configuration information for an ActiveMQ broker.
 *
 * @version $Revision$ $Date$
 */
public class BrokerConfiguration {
    private String name;
    private List<URI> networkConnectorUris;
    private List<TransportConnectorConfig> transportConnectorConfigs;
    private PersistenceAdapterConfig persistenceAdapter;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<URI> getNetworkConnectorUris() {
        return networkConnectorUris;
    }

    public void setNetworkConnectorUris(List<URI> networkConnectorUris) {
        this.networkConnectorUris = networkConnectorUris;
    }

    public List<TransportConnectorConfig> getTransportConnectorConfigs() {
        return transportConnectorConfigs;
    }

    public void setTransportConnectorConfigs(List<TransportConnectorConfig> transportConnectorConfigs) {
        this.transportConnectorConfigs = transportConnectorConfigs;
    }

    public PersistenceAdapterConfig getPersistenceAdapter() {
        return persistenceAdapter;
    }

    public void setPersistenceAdapter(PersistenceAdapterConfig adaptor) {
        this.persistenceAdapter = adaptor;
    }

}
