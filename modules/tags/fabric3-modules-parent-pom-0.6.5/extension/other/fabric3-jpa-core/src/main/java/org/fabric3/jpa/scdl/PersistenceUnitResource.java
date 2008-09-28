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
package org.fabric3.jpa.scdl;

import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.scdl.ServiceContract;

/**
 * Represents an entity manager factory treated as a resource.
 *
 * @version $Revision$ $Date$
 */
public final class PersistenceUnitResource extends ResourceDefinition {
    private static final long serialVersionUID = 8935762119919982256L;
    private final String unitName;

    /**
     * Initializes the resource name and persistence unit name.
     * 
     * @param name Name of the resource.
     * @param unitName Persistence unit name.
     * @param serviceContract the service contract for the persistence unit
     */
    public PersistenceUnitResource(String name, String unitName, ServiceContract<?> serviceContract) {
        super(name, serviceContract, true);
        this.unitName = unitName;
    }
    
    /**
     * Gets the persistence unit name.
     * @return Persistence unit name.
     */
    public final String getUnitName() {
        return this.unitName;
    }

}
