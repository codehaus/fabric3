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
package org.fabric3.fabric.marshaller;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.fabric.implementation.system.SystemImplementation;
import org.fabric3.loader.common.PropertyUtils;
import org.fabric3.spi.Constants;
import org.fabric3.pojo.processor.Introspector;
import org.fabric3.pojo.processor.PojoComponentType;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.model.type.Property;
import org.fabric3.spi.model.type.Scope;

/**
 * @version $Revision$ $Date$
 */
public class MarshallerLoader extends LoaderExtension<SystemImplementation> {

    private static final QName MARSHALLED = new QName(Constants.FABRIC3_SYSTEM_NS, "marshalled");

    private final DocumentBuilder builder;
    private Introspector introspector;

    /**
     * Registers the metadata loader with the registry.
     *
     * @param registry Loader registry.
     */
    public MarshallerLoader(LoaderRegistry registry, Introspector introspector) {
        super(registry);
        this.introspector = introspector;
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        try {
            builder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError();
        }
    }

    /**
     * Qualified name of the marshalled element.
     */
    @Override
    public QName getXMLType() {
        return MARSHALLED;
    }

    /**
     * Registers the metadata with the marshaller registry.
     */
    @SuppressWarnings("unchecked")
    public SystemImplementation load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {

        String modelClassName = reader.getAttributeValue(null, "modelClass");
        Class<?> modelClass = LoaderUtil.loadClass(modelClassName, context.getTargetClassLoader());

        String qname = reader.getAttributeValue(null, "xmlName");
        QName xmlName = QName.valueOf(qname);
        LoaderUtil.skipToEndElement(reader);

        final SystemImplementation marshallerImplementation = new SystemImplementation();
        final Class<ReflectionMarshaller> implClass = ReflectionMarshaller.class;
        marshallerImplementation.setImplementationClass(implClass);

        PojoComponentType componentType = getComponentType(implClass, context);

        Property<Class> modelClassProp = (Property<Class>) componentType.getProperties().get("modelClass");
        modelClassProp.setDefaultValue(PropertyUtils.createPropertyValue(modelClass.getName(), null, builder));
        // modelClassProp.setDefaultValueFactory(new SingletonObjectFactory<Class>(modelClass));

        Property<QName> xmlNameProp = (Property<QName>) componentType.getProperties().get("xmlName");
        xmlNameProp.setDefaultValue(PropertyUtils.createPropertyValue(xmlName.toString(), null, builder));
        // xmlNameProp.setDefaultValueFactory(new SingletonObjectFactory<QName>(xmlName));

        componentType.setImplementationScope(Scope.COMPOSITE);

        marshallerImplementation.setComponentType(componentType);

        return marshallerImplementation;

    }

    /*
    * Loads the component type for the reflection marshaller.
    */
    private PojoComponentType getComponentType(Class<ReflectionMarshaller> implClass, LoaderContext context)
            throws LoaderException {

        PojoComponentType componentType = new PojoComponentType(implClass);
        introspector.introspect(implClass, componentType, context);

        return componentType;

    }

}
