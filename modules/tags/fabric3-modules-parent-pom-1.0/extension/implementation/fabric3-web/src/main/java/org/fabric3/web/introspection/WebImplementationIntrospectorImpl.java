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
package org.fabric3.web.introspection;

import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.ClassWalker;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectionSite;
import org.fabric3.model.type.component.ReferenceDefinition;

/**
 * Default implementation of WebImplementationIntrospector.
 *
 * @version $Rev$ $Date$
 */
public class WebImplementationIntrospectorImpl implements WebImplementationIntrospector {
    private ClassWalker<WebArtifactImplementation> classWalker;
    private IntrospectionHelper helper;
    private WebXmlIntrospector xmlIntrospector;

    public WebImplementationIntrospectorImpl(@Reference(name = "classWalker")ClassWalker<WebArtifactImplementation> classWalker,
                                             @Reference(name = "xmlIntrospector")WebXmlIntrospector xmlIntrospector,
                                             @Reference(name = "helper")IntrospectionHelper helper) {
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
            PojoComponentType type = new PojoComponentType(artifact.getName());
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

    private void validateComponentType(PojoComponentType type, IntrospectionContext context) {
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
     * @param webType  the web component type to merge into
     * @param pojoType the POJO component to merge
     * @param context  the introspection context
     */
    private void mergeComponentTypes(WebComponentType webType, PojoComponentType pojoType, IntrospectionContext context) {
        for (Map.Entry<String, ReferenceDefinition> entry : pojoType.getReferences().entrySet()) {
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
        for (Map.Entry<InjectionSite, InjectableAttribute> entry : pojoType.getInjectionSites().entrySet()) {
            webType.addMapping(pojoType.getImplClass(), entry.getKey(), entry.getValue());
        }
    }

}