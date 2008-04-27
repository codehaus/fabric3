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
package org.fabric3.contribution.war;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;

import org.fabric3.spi.services.contribution.XmlIndexer;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.XmlIndexerRegistry;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.host.contribution.ContributionException;

/**
 * Adds an index entry for the web.xml descriptor to the symbol space of a WAR contribution.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class WebXmlIndexer implements XmlIndexer {
    private static final QName WEB_APP_NO_NAMESPACE = new QName(null, "web-app");
    private static final QName WEB_APP_NAMESPACE = new QName("http://java.sun.com/xml/ns/j2ee", "web-app");

    private XmlIndexerRegistry registry;
    private boolean namespace;

    public WebXmlIndexer(@Reference XmlIndexerRegistry registry, @Property(name = "namespace")boolean namespace) {
        this.registry = registry;
        this.namespace = namespace;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public QName getType() {
        if (namespace) {
            return WEB_APP_NAMESPACE;
        } else {
            return WEB_APP_NO_NAMESPACE;
        }
    }

    public void index(Resource resource, XMLStreamReader reader) throws ContributionException {
        QNameSymbol symbol;
        if (namespace) {
            symbol = new QNameSymbol(WEB_APP_NAMESPACE);
        } else {
            symbol = new QNameSymbol(WEB_APP_NO_NAMESPACE);
        }
        ResourceElement<QNameSymbol, Void> element = new ResourceElement<QNameSymbol, Void>(symbol);
        resource.addResourceElement(element);
    }
}