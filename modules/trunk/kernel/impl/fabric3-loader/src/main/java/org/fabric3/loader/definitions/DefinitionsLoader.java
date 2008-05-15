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
package org.fabric3.loader.definitions;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.scdl.definitions.AbstractDefinition;
import org.fabric3.scdl.definitions.BindingType;
import org.fabric3.scdl.definitions.ImplementationType;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.contribution.ResourceElementNotFoundException;
import org.fabric3.spi.services.contribution.Symbol;
import org.fabric3.spi.services.contribution.XmlResourceElementLoader;
import org.fabric3.spi.services.contribution.XmlResourceElementLoaderRegistry;

/**
 * Loader for definitions.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class DefinitionsLoader implements XmlResourceElementLoader {

    static final QName INTENT = new QName(SCA_NS, "intent");
    static final QName DESCRIPTION = new QName(SCA_NS, "description");
    static final QName POLICY_SET = new QName(SCA_NS, "policySet");
    static final QName BINDING_TYPE = new QName(SCA_NS, "bindingType");
    static final QName IMPLEMENTATION_TYPE = new QName(SCA_NS, "implementationType");

    private static final QName DEFINITIONS = new QName(SCA_NS, "definitions");

    private XmlResourceElementLoaderRegistry elementLoaderRegistry;
    private Loader loaderRegistry;

    public DefinitionsLoader(@Reference XmlResourceElementLoaderRegistry elementLoaderRegistry,
                             @Reference Loader loader) {
        this.elementLoaderRegistry = elementLoaderRegistry;
        this.loaderRegistry = loader;
    }

    @Init
    public void init() {
        elementLoaderRegistry.register(this);
    }

    public QName getType() {
        return DEFINITIONS;
    }

    public void load(XMLStreamReader reader, URI contributionUri, Resource resource, ClassLoader loader)
            throws ContributionException, XMLStreamException {

        List<AbstractDefinition> definitions = new ArrayList<AbstractDefinition>();

        String targetNamespace = reader.getAttributeValue(null, "targetNamespace");

        IntrospectionContext context = new DefaultIntrospectionContext(contributionUri, loader, targetNamespace);

        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                QName qname = reader.getName();
                AbstractDefinition definition = null;
                if (INTENT.equals(qname)) {
                    try {
                        definition = loaderRegistry.load(reader, Intent.class, context);
                    } catch (LoaderException e) {
                        throw new ContributionException(e);
                    }
                } else if (POLICY_SET.equals(qname)) {
                    try {
                        definition = loaderRegistry.load(reader, PolicySet.class, context);
                    } catch (LoaderException e) {
                        throw new ContributionException(e);
                    }
                } else if (BINDING_TYPE.equals(qname)) {
                    try {
                        definition = loaderRegistry.load(reader, BindingType.class, context);
                    } catch (LoaderException e) {
                        throw new ContributionException(e);
                    }
                } else if (IMPLEMENTATION_TYPE.equals(qname)) {
                    try {
                        definition = loaderRegistry.load(reader, ImplementationType.class, context);
                    } catch (LoaderException e) {
                        throw new ContributionException(e);

                    }
                }
                if (definition != null) {
                    definitions.add(definition);
                }
                break;
            case END_ELEMENT:
                assert DEFINITIONS.equals(reader.getName());
                // update indexed elements with the loaded definitions
                for (AbstractDefinition candidate : definitions) {
                    boolean found = false;
                    for (ResourceElement element : resource.getResourceElements()) {
                        Symbol candidateSymbol = new QNameSymbol(candidate.getName());
                        if (element.getSymbol().equals(candidateSymbol)) {
                            element.setValue(candidate);
                            found = true;
                        }
                    }
                    if (!found) {
                        String id = candidate.toString();
                        throw new ResourceElementNotFoundException("Definition not found: " + id, id);
                    }
                }
                return;
            }
        }

    }

}
