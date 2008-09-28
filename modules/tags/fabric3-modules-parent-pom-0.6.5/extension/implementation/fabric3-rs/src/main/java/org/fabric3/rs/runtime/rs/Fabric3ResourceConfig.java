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

import java.util.Set;

import com.sun.jersey.api.core.DefaultResourceConfig;

/**
 * @version $Rev$ $Date$
 */
public class Fabric3ResourceConfig extends DefaultResourceConfig {

    Fabric3ComponentProvider provider;

    public void setProvider(Fabric3ComponentProvider provider) {
        this.provider = provider;
    }

    // JFM - commented out as part of revision back to Jersey 0.9-ea 
    @Override
    public Set<Class<?>> getResourceClasses() {
        return provider.getClasses();
    }

}
