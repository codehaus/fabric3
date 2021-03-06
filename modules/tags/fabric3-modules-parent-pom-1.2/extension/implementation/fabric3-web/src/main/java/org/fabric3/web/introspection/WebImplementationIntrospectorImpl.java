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
package org.fabric3.web.introspection;

import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.model.type.java.InjectionSite;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.annotation.ClassWalker;
import org.fabric3.web.model.WebImplementation;

/**
 * Default implementation of WebImplementationIntrospector.
 *
 * @version $Rev$ $Date$
 */
public class WebImplementationIntrospectorImpl implements WebImplementationIntrospector {
    private ClassWalker<WebArtifactImplementation> classWalker;
    private IntrospectionHelper helper;
    private WebXmlIntrospector xmlIntrospector;

    public WebImplementationIntrospectorImpl(@Reference(name = "classWalker") ClassWalker<WebArtifactImplementation> classWalker,
                                             @Reference(name = "xmlIntrospector") WebXmlIntrospector xmlIntrospector,
                                             @Reference(name = "helper") IntrospectionHelper helper) {
        this.classWalker = classWalker;
        this.helper = helper;
        this.xmlIntrospector = xmlIntrospector;
    }

    public void introspect(WebImplementation implementation, IntrospectionContext context) {
        WebComponentType componentType = new WebComponentType();
        componentType.setScope("STATELESS");
        implementation.setComponentType(componentType);
        // load the servlet, filter and context listener classes referenced in the web.xml descriptor
        List<Class<?>> artifacts = xmlIntrospector.introspectArtifactClasses(context);
        for (Class<?> artifact : artifacts) {
            // introspect each class and generate a component type that will be merged into the web component type
            WebArtifactImplementation artifactImpl = new WebArtifactImplementation();
            InjectingComponentType type = new InjectingComponentType(artifact.getName());
            artifactImpl.setComponentType(type);
            TypeMapping typeMapping = helper.mapTypeParameters(artifact);
            IntrospectionContext childContext = new DefaultIntrospectionContext(context, typeMapping);
            classWalker.walk(artifactImpl, artifact, childContext);
            if (childContext.hasErrors()) {
                context.addErrors(childContext.getErrors());
            }
            if (childContext.hasWarnings()) {
                context.addWarnings(childContext.getWarnings());
            }
            validateComponentType(type, context);
            // TODO apply heuristics
            mergeComponentTypes(implementation.getComponentType(), type, context);
        }
    }

    private void validateComponentType(InjectingComponentType type, IntrospectionContext context) {
        for (ReferenceDefinition reference : type.getReferences().values()) {
            if (reference.getServiceContract().isConversational()) {
                IllegalConversationalReferenceInjection failure = new IllegalConversationalReferenceInjection(reference, type.getImplClass());
                context.addError(failure);
            }

        }
    }

    /**
     * Merges the POJO component type into the web component type.
     *
     * @param webType       the web component type to merge into
     * @param componentType the component type to merge
     * @param context       the introspection context
     */
    private void mergeComponentTypes(WebComponentType webType, InjectingComponentType componentType, IntrospectionContext context) {
        for (Map.Entry<String, ReferenceDefinition> entry : componentType.getReferences().entrySet()) {
            String name = entry.getKey();
            ReferenceDefinition reference = webType.getReferences().get(name);
            if (reference != null) {
                if (!reference.getServiceContract().isAssignableFrom(entry.getValue().getServiceContract())) {
                    // TODO display areas where it was not matching
                    IncompatibleReferenceDefinitions failure = new IncompatibleReferenceDefinitions(name);
                    context.addError(failure);
                }

            } else {
                webType.add(entry.getValue());
            }
        }
        // apply all injection sites
        for (Map.Entry<InjectionSite, InjectableAttribute> entry : componentType.getInjectionSites().entrySet()) {
            webType.addMapping(componentType.getImplClass(), entry.getKey(), entry.getValue());
        }
    }

}