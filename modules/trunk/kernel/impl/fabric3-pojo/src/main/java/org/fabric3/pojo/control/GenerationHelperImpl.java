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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;

import org.fabric3.pojo.provision.PojoComponentDefinition;
import org.fabric3.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.java.ConstructorInjectionSite;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectionSite;
import org.fabric3.model.type.java.Signature;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * @version $Rev$ $Date$
 */
public class GenerationHelperImpl implements InstanceFactoryGenerationHelper {

    public Integer getInitLevel(ComponentDefinition<?> definition, PojoComponentType type) {
        Integer initLevel = definition.getInitLevel();
        if (initLevel == null) {
            initLevel = type.getInitLevel();
        }
        return initLevel;
    }

    public Signature getSignature(Method method) {
        return method == null ? null : new Signature(method);
    }

    public void processInjectionSites(LogicalComponent<? extends Implementation<PojoComponentType>> component,
                                      InstanceFactoryDefinition providerDefinition) {

        Implementation<PojoComponentType> implementation = component.getDefinition().getImplementation();
        PojoComponentType type = implementation.getComponentType();
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
