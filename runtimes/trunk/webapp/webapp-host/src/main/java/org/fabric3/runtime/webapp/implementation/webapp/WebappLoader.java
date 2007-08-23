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
package org.fabric3.runtime.webapp.implementation.webapp;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.Scope;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.ComponentType;
import org.fabric3.spi.idl.InvalidServiceContractException;
import org.fabric3.spi.idl.java.InterfaceJavaIntrospector;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.MissingResourceException;
import org.fabric3.spi.loader.UnrecognizedElementException;

/**
 * @version $Rev$ $Date$
 */
public class WebappLoader extends LoaderExtension<WebappImplementation> {

    private final InterfaceJavaIntrospector introspector;

    public WebappLoader(@Reference LoaderRegistry registry,
                        @Reference InterfaceJavaIntrospector introspector) {
        super(registry);
        this.introspector = introspector;
    }

    public QName getXMLType() {
        return WebappImplementation.IMPLEMENTATION_WEBAPP;
    }

    public WebappImplementation load(XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {

        ComponentType componentType = new ComponentType();
        componentType.setImplementationScope(Scope.COMPOSITE);
        WebappImplementation impl = new WebappImplementation();
        impl.setComponentType(componentType);

        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                QName qname = reader.getName();
                if ("reference".equals(qname.getLocalPart())) {
                    defineReference(componentType, reader, loaderContext);
                } else {
                    throw new UnrecognizedElementException(qname);
                }
                reader.next();
                break;
            case END_ELEMENT:
                return impl;
            }
        }
    }

    protected void defineReference(AbstractComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> componentType,
                                   XMLStreamReader reader,
                                   LoaderContext context) throws LoaderException {
        String name = reader.getAttributeValue(null, "name");

        String className = reader.getAttributeValue(null, "interface");
        if (className == null) {
            className = reader.getAttributeValue(null, "class");
        }

        Class<?> referenceType;
        try {
            referenceType = context.getTargetClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new MissingResourceException(className, e);
        }

        ServiceContract serviceContract;
        try {
            serviceContract = introspector.introspect(referenceType);
        } catch (InvalidServiceContractException e) {
            throw new ProcessingException("Invalid service contract", name, e);
        }

        ReferenceDefinition definition = new ReferenceDefinition(name, serviceContract);
        componentType.add(definition);
    }
}
