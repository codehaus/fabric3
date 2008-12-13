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
package org.fabric3.rs.runtime.rs;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;

/**
 * @version $Rev$ $Date$
 */
public class RsServlet extends ServletContainer {

    Fabric3ComponentProvider componentProvider;

    public RsServlet(Fabric3ComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    @Override
    protected void initiate(ResourceConfig rc, WebApplication wa) {
        if (rc instanceof Fabric3ResourceConfig) {
            Fabric3ResourceConfig f3rc = (Fabric3ResourceConfig) rc;
            f3rc.setProvider(componentProvider);
            wa.initiate(rc, componentProvider);
        } else {
            wa.initiate(rc);
        }

    }
}
