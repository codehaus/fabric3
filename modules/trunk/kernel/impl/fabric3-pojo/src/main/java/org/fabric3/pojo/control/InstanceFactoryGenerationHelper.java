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
package org.fabric3.pojo.control;

import java.lang.reflect.Method;

import org.fabric3.pojo.provision.PojoComponentDefinition;
import org.fabric3.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Signature;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * @version $Rev$ $Date$
 */
public interface InstanceFactoryGenerationHelper {
    Integer getInitLevel(ComponentDefinition<?> definition, PojoComponentType type);

    Signature getSignature(Method method);

    void processInjectionSites(LogicalComponent<? extends Implementation<PojoComponentType>> component, InstanceFactoryDefinition providerDefinition);

    /**
     * Set the actual values of the physical properties.
     *
     * @param component the component corresponding to the implementation
     * @param physical  the physical component whose properties should be set
     */
    void processPropertyValues(LogicalComponent<?> component, PojoComponentDefinition physical);
}
