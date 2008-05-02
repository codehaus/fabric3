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
package org.fabric3.loader.xmlcontribution;

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
import org.fabric3.scdl.Composite;
import static org.fabric3.spi.Constants.FABRIC3_NS;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.contribution.XmlIndexer;
import org.fabric3.spi.services.contribution.XmlIndexerRegistry;

/**
 * Indexer for the <xmlContribution> type.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class XmlContributionTypeIndexer implements XmlIndexer {
    private static final QName XML_CONTRIBUTION = new QName(FABRIC3_NS, "xmlContribution");
    private static final QName COMPOSITE = new QName(SCA_NS, "composite");
    private XmlIndexerRegistry registry;


    public XmlContributionTypeIndexer(@Reference XmlIndexerRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public QName getType() {
        return XML_CONTRIBUTION;
    }

    public void index(Resource resource, XMLStreamReader reader) throws ContributionException {

        while (true) {
            try {
                switch (reader.next()) {
                case START_ELEMENT:
                    QName qname = reader.getName();
                    if (COMPOSITE.equals(qname)) {
                        String name = reader.getAttributeValue(null, "name");
                        String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
                        QName compositeName = new QName(targetNamespace, name);
                        QNameSymbol symbol = new QNameSymbol(compositeName);
                        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol);
                        resource.addResourceElement(element);
                        break;
                    } else {
                        // unknown element, just skip
                        continue;
                    }
                case XMLStreamConstants.END_DOCUMENT:
                    return;
                }
            } catch (XMLStreamException e) {
                throw new ContributionException("Error processing resource: " + resource.getUrl().toString(), e);
            }
        }

    }

}