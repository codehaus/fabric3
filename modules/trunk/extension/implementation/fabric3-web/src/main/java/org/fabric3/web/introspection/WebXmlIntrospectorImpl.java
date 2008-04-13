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
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLInputFactory;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.services.xmlfactory.XMLFactory;

/**
 * Default implementation of WebXmlIntrospector.
 *
 * @version $Revision$ $Date$
 */
public class WebXmlIntrospectorImpl implements WebXmlIntrospector {
    private static final QNameSymbol WEB_APP_NO_NAMESPACE = new QNameSymbol(new QName(null, "web-app"));
    private static final QNameSymbol WEB_APP_NAMESPACE = new QNameSymbol(new QName("http://java.sun.com/xml/ns/j2ee", "web-app"));

    private MetaDataStore store;
    private XMLInputFactory xmlFactory;

    public WebXmlIntrospectorImpl(@Reference MetaDataStore store, @Reference XMLFactory factory) {
        this.store = store;
        this.xmlFactory = factory.newInputFactoryInstance();
    }

    public List<Class<?>> introspectArtifactClasses(IntrospectionContext context) throws IntrospectionException {
        List<Class<?>> artifacts = new ArrayList<Class<?>>();
        ClassLoader cl = context.getTargetClassLoader();
        Resource resource = store.resolveContainingResource(context.getContributionUri(), WEB_APP_NAMESPACE);
        if (resource == null) {
            resource = store.resolveContainingResource(context.getContributionUri(), WEB_APP_NO_NAMESPACE);
            if (resource == null) {
                // tolerate no web.xml
                return artifacts;
            }
        }
        InputStream xmlStream = null;
        try {
            xmlStream = resource.getUrl().openStream();
            XMLStreamReader reader = xmlFactory.createXMLStreamReader(xmlStream);
            while (true) {
                // only match on local part since namespaces may be omitted
                int event = reader.next();
                switch (event) {
                case START_ELEMENT:
                    String name = reader.getName().getLocalPart();
                    if (name.equals("servlet-class")) {
                        String className = reader.getElementText();
                        artifacts.add(cl.loadClass(className.trim()));
                    } else if (name.equals("filter-class")) {
                        String className = reader.getElementText();
                        artifacts.add(cl.loadClass(className.trim()));
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getName().getLocalPart().equals("web-app")) {
                        return artifacts;
                    }
                    break;
                case END_DOCUMENT:
                    return artifacts;
                }
            }
        } catch (XMLStreamException e) {
            throw new InvalidWebManifestException("Error reading web.xml", e);
        } catch (ClassNotFoundException e) {
            throw new InvalidWebManifestException("Class referenced in web.xml not found", e);
        } catch (IOException e) {
            throw new InvalidWebManifestException("Class referenced in web.xml not found", e);
        } finally {
            try {
                if (xmlStream != null) {
                    xmlStream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }

    }
}
