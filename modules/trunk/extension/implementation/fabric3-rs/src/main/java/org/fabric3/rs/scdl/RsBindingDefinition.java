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
package org.fabric3.rs.scdl;

import java.net.URI;
import javax.xml.namespace.QName;
import org.fabric3.scdl.BindingDefinition;

/**
 * @version $Rev$ $Date$
 */
public class RsBindingDefinition extends BindingDefinition {

    public static final QName BINDING_RS = new QName("http://www.fabric3.org/xmlns/rs/1.0", "binding.rs");
    private boolean isResource;
    private boolean isProvider;

    public RsBindingDefinition(URI targetUri) {
        super(targetUri, BINDING_RS, null);
    }

    public boolean isProvider() {
        return isProvider;
    }

    public void setIsProvider(boolean value) {
        this.isProvider = value;
    }

    public boolean isResource() {
        return isResource;
    }

    public void setIsResource(boolean value) {
        this.isResource = value;
    }
}
