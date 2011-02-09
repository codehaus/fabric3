/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.management.rest.framework.domain.contribution;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.SelfLink;

/**
 * A contribution in the domain.
 *
 * @version $Rev: 9923 $ $Date: 2011-02-03 17:11:06 +0100 (Thu, 03 Feb 2011) $
 */
public class ContributionResource {
    private URI uri;
    private SelfLink selfLink;
    private String state;
    private QName deployables;

    /**
     * Constructor.
     *
     * @param uri         the contribution URI
     * @param state       the contribution state
     * @param deployables the qualified names of deployable composites contained in the contribution
     * @param selfLink    the resource self link
     */
    public ContributionResource(URI uri, SelfLink selfLink, String state, QName deployables) {
        this.selfLink = selfLink;
        this.uri = uri;
        this.state = state;
        this.deployables = deployables;
    }

    /**
     * Returns the contribution URI.
     *
     * @return the contribution URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the contribution resource self link.
     *
     * @return the contribution resource self link
     */
    public Link getSelfLink() {
        return selfLink;
    }

    /**
     * Returns the contribution state.
     *
     * @return the contribution state
     */
    public String getState() {
        return state;
    }

    /**
     * Returns the qualified names of deployable composites contained in the contribution
     *
     * @return the qualified names of deployable composites contained in the contribution
     */
    public QName getDeployables() {
        return deployables;
    }

}
