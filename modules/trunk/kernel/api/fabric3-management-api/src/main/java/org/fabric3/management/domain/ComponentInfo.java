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
package org.fabric3.management.domain;

import java.io.Serializable;
import java.net.URI;
import javax.xml.namespace.QName;

/**
 * Encapsulates information about a component.
 *
 * @version $Revision$ $Date$
 */
public class ComponentInfo implements Serializable {
    private static final long serialVersionUID = -4847696410167502510L;
    private URI uri;
    private QName deployable;
    private String zone;

    public ComponentInfo(URI uri, QName deployable, String zone) {
        this.uri = uri;
        this.deployable = deployable;
        this.zone = zone;
    }

    public URI getUri() {
        return uri;
    }

    public QName getDeployable() {
        return deployable;
    }

    public String getZone() {
        return zone;
    }
}
