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
package org.fabric3.fabric.instantiator.component;

import java.net.URI;

import org.fabric3.host.domain.AssemblyFailure;

/**
 * @version $Rev: 4789 $ $Date: 2008-06-08 07:54:46 -0700 (Sun, 08 Jun 2008) $
 */
public class PropertySourceNotFound extends AssemblyFailure {
    private String name;

    public PropertySourceNotFound(URI componentURI, String name) {
        super(componentURI);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return "The source XPath expression for property " + name + " in component " + getComponentUri() + " returned an empty value";
    }
}