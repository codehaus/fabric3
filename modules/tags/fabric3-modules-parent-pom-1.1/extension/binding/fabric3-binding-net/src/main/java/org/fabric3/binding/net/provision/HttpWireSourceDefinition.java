/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.net.provision;

import org.fabric3.binding.net.config.HttpConfig;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 * Source definition for the HTTP binding.
 *
 * @version $Revision$ $Date$
 */
public class HttpWireSourceDefinition extends PhysicalWireSourceDefinition {
    private static final long serialVersionUID = 3519868912711758321L;
    private HttpConfig config;


    public HttpConfig getConfig() {
        return config;
    }

    public void setConfig(HttpConfig config) {
        this.config = config;
    }

}
