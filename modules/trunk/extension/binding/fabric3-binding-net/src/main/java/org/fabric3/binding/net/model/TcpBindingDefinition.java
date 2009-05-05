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
package org.fabric3.binding.net.model;

import java.net.URI;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import org.w3c.dom.Document;

import org.fabric3.binding.net.config.TcpConfig;
import org.fabric3.host.Namespaces;
import org.fabric3.model.type.component.BindingDefinition;

/**
 * Represents a binding.http configuration.
 *
 * @version $Revision$ $Date$
 */
public class TcpBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = 1035192281713003125L;
    public static final QName TCP_BINDING = new QName(Namespaces.BINDING, "binding.tcp");
    private TcpConfig config;

    public TcpBindingDefinition(URI targetUri, Document key) {
        super(targetUri, new QName(Constants.SCA_NS, "binding.tcp"), key);
        config = new TcpConfig();
    }

    public TcpConfig getConfig() {
        return config;
    }

}