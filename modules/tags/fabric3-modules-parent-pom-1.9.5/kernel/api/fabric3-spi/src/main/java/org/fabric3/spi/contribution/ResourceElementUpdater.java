/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.spi.contribution;

import java.io.Serializable;
import java.util.Set;

import org.fabric3.model.type.ModelObject;

/**
 * Dynamically updates a resource element contained in contribution and all references to it, including the transitive set of importing contributions,
 * if any.
 *
 * @version $Rev$ $Date$
 */
public interface ResourceElementUpdater<V extends Serializable> {

    /**
     * Updates the resource element with the new value.
     *
     * @param value                  the new resource element value
     * @param contribution           the containing contribution
     * @param dependentContributions the transitive set of dependent contributions
     * @return the collection of model object that have been changed by the update. For example, an update to a composite will cause changes in other
     *         composites that reference it
     */
    Set<ModelObject> update(V value, Contribution contribution, Set<Contribution> dependentContributions);

    /**
     * Removes a resource element from a contribution. References to the element may be replaced by unresolved pointers depending on the resource
     * type.
     *
     * @param value                  the resource element value to remove
     * @param contribution           the containing contribution
     * @param dependentContributions the transitive set of dependent contributions
     * @return the collection of model object that have been changed by the removal. For example, a deleted composite will cause changes in other
     *         composites that reference it. References to deleted elements may be replaced with pointers.
     */
    Set<ModelObject> remove(V value, Contribution contribution, Set<Contribution> dependentContributions);

}
