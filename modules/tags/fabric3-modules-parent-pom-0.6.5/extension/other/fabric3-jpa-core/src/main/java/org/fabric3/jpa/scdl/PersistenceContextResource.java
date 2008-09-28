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

import javax.persistence.PersistenceContextType;

import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.scdl.ServiceContract;

/**
 * Represents an entity manager factory treated as a resource.
 *
 * @version $Revision$ $Date$
 */
public final class PersistenceContextResource extends ResourceDefinition {
    private static final long serialVersionUID = -8717050996527626286L;
    private final String unitName;
    private final PersistenceContextType type;
    private final boolean multiThreaded;

    /**
     * Constructor.
     *
     * @param name            Name of the resource.
     * @param unitName        Persistence unit name.
     * @param type            the PersistenceContextType
     * @param serviceContract the service contract for the persistence unit
     * @param multiThreaded   true if the resource is accessed from a multi-threaded implementation
     */
    public PersistenceContextResource(String name,
                                      String unitName,
                                      PersistenceContextType type,
                                      ServiceContract<?> serviceContract,
                                      boolean multiThreaded) {
        super(name, serviceContract, true);
        this.unitName = unitName;
        this.type = type;
        this.multiThreaded = multiThreaded;
    }

    /**
     * Returns the persistence unit name.
     *
     * @return the persistence unit name.
     */
    public final String getUnitName() {
        return this.unitName;
    }

    /**
     * Returns the persistence context type.
     *
     * @return the persistence context type
     */
    public PersistenceContextType getType() {
        return type;
    }

    /**
     * Returns true if the EntityManager will be accessed from a mutli-thread implementation.
     *
     * @return true if the EntityManager will be accessed from a mutli-thread implementation
     */
    public boolean isMultiThreaded() {
        return multiThreaded;
    }
}