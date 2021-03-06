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
package org.fabric3.implementation.drools.introspection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.ResourceType;
import org.drools.builder.impl.KnowledgeBuilderImpl;
import org.drools.compiler.PackageBuilder;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.osoa.sca.annotations.Reference;

import org.fabric3.implementation.drools.model.DroolsImplementation;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.ResourceNotFound;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Loads a Drools component implementation in a composite.
 *
 * @version $Rev$ $Date$
 */
public class DroolsImplementationLoader implements TypeLoader<DroolsImplementation> {
    private static final String IMPLEMENTATION_DROOLS = "implementation.drools";
    private static final String SERVICE = "service";
    private static final String RESOURCE = "resource";

    private RulesIntrospector rulesIntrospector;

    public DroolsImplementationLoader(@Reference RulesIntrospector rulesIntrospector) {
        this.rulesIntrospector = rulesIntrospector;
    }

    public DroolsImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        List<String> resources = new ArrayList<String>();
        Map<String, Class<?>> services = new HashMap<String, Class<?>>();

        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                QName qname = reader.getName();
                if (SERVICE.equals(qname.getLocalPart())) {
                    parseServices(services, reader, context);
                } else if (RESOURCE.equals(qname.getLocalPart())) {
                    parseResources(resources, reader, context);
                } else {
                    // Unknown extension element - issue an error and continue
                    context.addError(new UnrecognizedElement(reader));
                    LoaderUtil.skipToEndElement(reader);
                }
                break;
            case END_ELEMENT:
                if (IMPLEMENTATION_DROOLS.equals(reader.getName().getLocalPart())) {
                    if (resources.isEmpty()) {
                        MissingKnowledgeBaseDefinition error = new MissingKnowledgeBaseDefinition(reader);
                        context.addError(error);
                        // mock up an implementation to allow processing to continue
                        ComponentType componentType = new ComponentType();
                        return new DroolsImplementation(componentType, Collections.<KnowledgePackage>emptyList());
                    }

                    KnowledgeBuilderImpl builder = createBuilder(resources, reader, context);
                    Map<String, Class<?>> globals = builder.getPackageBuilder().getGlobals();

                    ComponentType componentType = rulesIntrospector.introspect(services, globals, context);

                    Collection<KnowledgePackage> knowledgePackages = builder.getKnowledgePackages();

                    return new DroolsImplementation(componentType, knowledgePackages);
                }

            }
        }
    }

    private void parseServices(Map<String, Class<?>> services, XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String service = reader.getAttributeValue(null, "interface");
        if (service == null) {
            MissingAttribute error =
                    new MissingAttribute("The interface attribute must be specified for a knowledge base service", reader);
            context.addError(error);
            LoaderUtil.skipToEndElement(reader);
        }
        Class<?> serviceType;
        try {
            serviceType = context.getClassLoader().loadClass(service);
            String name = reader.getAttributeValue(null, "name");
            if (name == null) {
                name = serviceType.getSimpleName();
            }
            services.put(name, serviceType);
        } catch (ClassNotFoundException e) {
            ResourceNotFound failure = new ResourceNotFound("Interface not found: " + service, reader);
            context.addError(failure);
        }
    }

    private void parseResources(List<String> resources, XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String source = reader.getAttributeValue(null, "source");
        if (source == null) {
            MissingAttribute error = new MissingAttribute("The source attribute must be specified for a knowledge base resource", reader);
            context.addError(error);
            LoaderUtil.skipToEndElement(reader);
        }
        resources.add(source);
    }

    private KnowledgeBuilderImpl createBuilder(List<String> resources, XMLStreamReader reader, IntrospectionContext context) {
        PackageBuilder packageBuilder = new PackageBuilder();
        KnowledgeBuilderImpl builder = new KnowledgeBuilderImpl(packageBuilder);
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            for (String resourceString : resources) {
                Resource resource = ResourceFactory.newClassPathResource(resourceString, context.getClassLoader());
                builder.add(resource, ResourceType.DRL);
                if (builder.hasErrors()) {
                    KnowledgeBuilderErrors errors = builder.getErrors();
                    KnowledgeError error = new KnowledgeError(errors, reader);
                    context.addError(error);
                    break;
                }
            }
        } catch (RuntimeException e) {
            // Drools throws generic RuntimeExceptions for conditions such as FileNotFound for a resource
            RulesParsingError error = new RulesParsingError("Error parsing rules", e, reader);
            context.addError(error);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
        return builder;

    }


    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"requires".equals(name) && !"policySets".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }


}
