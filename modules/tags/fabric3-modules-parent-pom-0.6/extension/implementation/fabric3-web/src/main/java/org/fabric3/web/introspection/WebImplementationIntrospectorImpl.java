/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.fabric3.web.introspection;

import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.java.ClassWalker;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.ReferenceDefinition;

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