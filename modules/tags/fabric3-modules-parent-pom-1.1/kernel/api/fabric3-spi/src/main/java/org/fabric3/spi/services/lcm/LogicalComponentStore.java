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
package org.fabric3.spi.services.lcm;

import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Implementations persist the logical SCA domain model
 *
 * @version $Rev$ $Date$
 */
public interface LogicalComponentStore {

    /**
     * Stores the domain model.
     *
     * @param domain the domain model
     * @throws WriteException if an error occurs storing the domain
     */
    void store(LogicalCompositeComponent domain) throws WriteException;

    /**
     * Reads the domain model from the store.
     *
     * @return the domain model from the store
     * @throws ReadException f an error occurs reading from the store
     */
    LogicalCompositeComponent read() throws ReadException;

}
