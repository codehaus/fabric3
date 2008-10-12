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
package org.fabric3.binding.ejb.scdl;

import java.net.URI;

import org.fabric3.binding.ejb.introspection.EjbBindingLoader;
import org.fabric3.scdl.BindingDefinition;

/**
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public class EjbBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = -6107882088582151893L;
    private String homeInterface;
    private String ejbLink;
    private boolean isStateless = true;
    private boolean isEjb3;
    private String name;

    public EjbBindingDefinition(URI targetUri, String key) {
        super(targetUri, EjbBindingLoader.BINDING_QNAME, key);
    }

    //TODO PolicySets & Requires

    public String getHomeInterface() {
        return homeInterface;
    }

    public void setHomeInterface(String homeInterface) {
        this.homeInterface = homeInterface;
    }

    public String getEjbLink() {
        return ejbLink;
    }

    public void setEjbLink(String ejbLink) {
        this.ejbLink = ejbLink;
    }

    public boolean isStateless() {
        return isStateless;
    }

    public void setStateless(boolean stateless) {
        isStateless = stateless;
    }

    public boolean isEjb3() {
        return isEjb3;
    }

    public void setEjb3(boolean Ejb3) {
        isEjb3 = Ejb3;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
