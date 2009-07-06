/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.pojo.generator;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.java.ConstructorInjectionSite;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.model.type.java.InjectionSite;
import org.fabric3.model.type.java.Signature;
import org.fabric3.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.pojo.provision.PojoComponentDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * @version $Rev$ $Date$
 */
public class GenerationHelperImpl implements InstanceFactoryGenerationHelper {

    public Integer getInitLevel(ComponentDefinition<?> definition, InjectingComponentType type) {
        Integer initLevel = definition.getInitLevel();
        if (initLevel == null) {
            initLevel = type.getInitLevel();
        }
        return initLevel;
    }

    public Signature getSignature(Method method) {
        return method == null ? null : new Signature(method);
    }

    public void processInjectionSites(LogicalComponent<? extends Implementation<InjectingComponentType>> component,
                                      InstanceFactoryDefinition providerDefinition) {

        Implementation<InjectingComponentType> implementation = component.getDefinition().getImplementation();
        InjectingComponentType type = implementation.getComponentType();
        Map<InjectionSite, InjectableAttribute> mappings = type.getInjectionSites();

        // add injections for all the active constructor args
        Map<InjectionSite, InjectableAttribute> construction = providerDefinition.getConstruction();
        Signature constructor = type.getConstructor();
        Set<InjectableAttribute> byConstruction = new HashSet<InjectableAttribute>(constructor.getParameterTypes().size());
        for (int i = 0; i < constructor.getParameterTypes().size(); i++) {
            InjectionSite site = new ConstructorInjectionSite(constructor, i);
            InjectableAttribute attribute = mappings.get(site);
            construction.put(site, attribute);
            byConstruction.add(attribute);
        }

        // add field/method injections
        Map<InjectionSite, InjectableAttribute> postConstruction = providerDefinition.getPostConstruction();
        Map<InjectionSite, InjectableAttribute> reinjection = providerDefinition.getReinjection();
        for (Map.Entry<InjectionSite, InjectableAttribute> entry : mappings.entrySet()) {
            InjectionSite site = entry.getKey();
            if (site instanceof ConstructorInjectionSite) {
                continue;
            }

            InjectableAttribute attribute = entry.getValue();
            if (!byConstruction.contains(attribute)) {
                postConstruction.put(site, attribute);
            }
            reinjection.put(site, attribute);
        }
    }

    public void processPropertyValues(LogicalComponent<?> component, PojoComponentDefinition physical) {
        for (Map.Entry<String, Document> entry : component.getPropertyValues().entrySet()) {
            String name = entry.getKey();
            Document value = entry.getValue();
            if (value != null) {
                physical.setPropertyValue(name, value);
            }
        }
    }
}
