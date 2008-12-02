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
package org.fabric3.binding.rmi.model.logical;

import java.net.URI;

import org.fabric3.scdl.BindingDefinition;
import org.w3c.dom.Document;

public class RmiBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = 5023496186591172203L;

    private static final boolean DEBUG = false;
    private String name;
    private int port = 7701;
    private String host = "localhost";
    private String serviceName;

    public RmiBindingDefinition(URI targetUri, Document key) {
        super(targetUri, RmiBindingLoader.BINDING_QNAME, key);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String toString() {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder("Name: ");
            sb.append(name).append(" Service Name: ").append(serviceName);
            sb.append(" Host: ").append(host).append(" Port: ").append(port);
            return sb.toString();
        }
        return name;
    }


    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

}
