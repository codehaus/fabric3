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
package org.fabric3.web.provision;

import org.fabric3.model.type.java.InjectionSite;

/**
 * An injection site specialized for web applications.
 *
 * @version $Revision$ $Date$
 */
public class WebContextInjectionSite extends InjectionSite {
    private static final long serialVersionUID = 8530588154179239645L;
    private ContextType contextType;

    public static enum ContextType {
        SERVLET_CONTEXT,
        SESSION_CONTEXT
    }

    public WebContextInjectionSite(String type, ContextType contextType) {
        super(type);
        this.contextType = contextType;
    }

    public ContextType getContextType() {
        return contextType;
    }


}
