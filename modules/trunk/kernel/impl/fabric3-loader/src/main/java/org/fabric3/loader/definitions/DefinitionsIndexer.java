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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.scdl.definitions.AbstractDefinition;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.contribution.XmlIndexer;
import org.fabric3.spi.services.contribution.XmlIndexerRegistry;

/**
 * Indexer for definitions.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class DefinitionsIndexer implements XmlIndexer {
    private static final QName DEFINITIONS = new QName(SCA_NS, "definitions");
    private static final QName INTENT = new QName(SCA_NS, "intent");
    private static final QName POLICY_SET = new QName(SCA_NS, "policySet");
    private static final QName BINDING_TYPE = new QName(SCA_NS, "bindingType");
    private static final QName IMPLEMENTATION_TYPE = new QName(SCA_NS, "implementationType");
    private XmlIndexerRegistry registry;


    public DefinitionsIndexer(@Reference XmlIndexerRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public QName getType() {
        return DEFINITIONS;
    }

    public void index(Resource resource, XMLStreamReader reader) throws ContributionException {
        String targetNamespace = reader.getAttributeValue(null, "targetNamespace");

        while (true) {
            try {
                switch (reader.next()) {
                case START_ELEMENT:
                    QName qname = reader.getName();
                    if (!INTENT.equals(qname)
                            && !POLICY_SET.equals(qname)
                            && !BINDING_TYPE.equals(qname)
                            && !IMPLEMENTATION_TYPE.equals(qname)) {
                        continue;
                    }
                    String nameAttr = reader.getAttributeValue(null, "name");
                    NamespaceContext namespaceContext = reader.getNamespaceContext();
                    QName name = LoaderUtil.getQName(nameAttr, targetNamespace, namespaceContext);
                    QNameSymbol symbol = new QNameSymbol(name);
                    ResourceElement<QNameSymbol, AbstractDefinition> element =
                            new ResourceElement<QNameSymbol, AbstractDefinition>(symbol);
                    resource.addResourceElement(element);
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    return;
                }
            } catch (XMLStreamException e) {
                throw new ContributionException(e);
            }
        }

    }

}