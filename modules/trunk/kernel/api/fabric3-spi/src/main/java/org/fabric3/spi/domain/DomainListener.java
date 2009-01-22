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
package org.fabric3.spi.domain;

import javax.xml.namespace.QName;

/**
 * Implementations receive callbacks for domain events.
 *
 * @version $Revision$ $Date$
 */
public interface DomainListener {

    /**
     * Called when a composite is included in the domain.
     *
     * @param deployable the composite qualified name
     * @param plan       the deployment plan or null if none is specified
     */
    void onInclude(QName deployable, String plan);

    /**
     * Called when a composite is undeployed from the domain.
     *
     * @param undeployed the composite qualified name
     */
    void onUndeploy(QName undeployed);
}
