/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.rs.introspection;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.ImplementationProcessor;
import org.fabric3.introspection.xml.InvalidValue;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.rs.scdl.RsBindingDefinition;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class RsImplementationLoader implements TypeLoader<JavaImplementation> {

    private final LoaderHelper loaderHelper;
    private final ImplementationProcessor processor;
    private final RsHeuristic rsHeuristic;

    public RsImplementationLoader(@Reference(name = "implementationProcessor") ImplementationProcessor processor,
            @Reference(name = "RsHeuristic") RsHeuristic rsHeuristic,
            @Reference LoaderHelper loaderHelper) {
        this.processor = processor;
        this.loaderHelper = loaderHelper;
        this.rsHeuristic = rsHeuristic;
    }

    public JavaImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        String className = reader.getAttributeValue(null, "class");
        String webApp = reader.getAttributeValue(null, "uri");
        URI webAppURI = null;

        if (className == null) {
            MissingAttribute failure = new MissingAttribute("No class name specified", "class", reader);
            context.addError(failure);
            return null;
        }

        if (webApp == null) {
            MissingAttribute failure = new MissingAttribute("No web application URI specified", "uri", reader);
            context.addError(failure);
            return null;
        }
        try {
            webAppURI = new URI(webApp);
        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("invalid URI value", "uri", reader);
            context.addError(failure);
            return null;
        }

        JavaImplementation impl = new JavaImplementation();
        impl.setImplementationClass(className);
        loaderHelper.loadPolicySetsAndIntents(impl, reader, context);
        processor.introspect(impl, context);
        LoaderUtil.skipToEndElement(reader);

        RsBindingDefinition bindingDefinition = new RsBindingDefinition(webAppURI);
        rsHeuristic.applyHeuristics(bindingDefinition, className, context);

        ServiceDefinition definition = new ServiceDefinition("REST");
        final String intName = className;
        ServiceContract serviceContract = new ServiceContract() {

            @Override
            public boolean isAssignableFrom(ServiceContract contract) {
                return false;
            }

            @Override
            public String getQualifiedInterfaceName() {
                return intName;
            }
        };
        serviceContract.setInterfaceName(intName);
        definition.setServiceContract(serviceContract);
        definition.addBinding(bindingDefinition);
        InjectingComponentType componentType = impl.getComponentType();
        componentType.add(definition);
        return impl;
    }
}
