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
package org.fabric3.jpa.generator;

import javax.persistence.PersistenceContextType;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.jpa.common.PersistenceOverrides;
import org.fabric3.jpa.model.HibernateSessionResourceReference;
import org.fabric3.jpa.override.OverrideRegistry;
import org.fabric3.jpa.provision.SessionTargetDefinition;
import org.fabric3.spi.generator.ResourceReferenceGenerator;
import org.fabric3.spi.model.instance.LogicalResourceReference;

/**
 * @version $Rev: 7665 $ $Date: 2009-09-21 11:11:12 +0200 (Mon, 21 Sep 2009) $
 */
@EagerInit
public class SessionResourceReferenceGenerator implements ResourceReferenceGenerator<HibernateSessionResourceReference> {
    private OverrideRegistry registry;

    public SessionResourceReferenceGenerator(@Reference OverrideRegistry registry) {
        this.registry = registry;
    }

    public SessionTargetDefinition generateWireTarget(LogicalResourceReference<HibernateSessionResourceReference> logicalResourceReference) {
        HibernateSessionResourceReference resource = logicalResourceReference.getDefinition();
        String unitName = resource.getUnitName();
        SessionTargetDefinition definition = new SessionTargetDefinition(unitName);

        PersistenceOverrides overrides = registry.resolve(unitName);
        if (overrides != null) {
            definition.setOverrides(overrides);
        }

        boolean multiThreaded = resource.isMultiThreaded();
        boolean extended = PersistenceContextType.EXTENDED == resource.getType();
        definition.setOptimizable(true);
        definition.setExtended(extended);
        definition.setMultiThreaded(multiThreaded);
        return definition;
    }

}