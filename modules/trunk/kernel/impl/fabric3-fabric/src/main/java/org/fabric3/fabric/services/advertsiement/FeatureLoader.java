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
package org.fabric3.fabric.services.advertsiement;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Destroy;
import org.w3c.dom.Document;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.fabric.implementation.system.SystemImplementation;
import org.fabric3.loader.common.PropertyHelper;
import org.fabric3.pojo.processor.Introspector;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.PropertyValue;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.Constants;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.StAXElementLoader;

/**
 * @version $Revision: 1 $ $Date: 2007-05-14 18:40:37 +0100 (Mon, 14 May 2007) $
 */
public class FeatureLoader implements StAXElementLoader<ComponentDefinition> {

    // Qualified name of the root element.
    private static final QName QNAME = new QName(Constants.FABRIC3_SYSTEM_NS, "feature");

    private LoaderRegistry registry;
    // Introspector
    private final Introspector introspector;

    private final PropertyHelper propertyHelper;

    public FeatureLoader(@Reference LoaderRegistry registry, @Reference Introspector introspector, @Reference PropertyHelper propertyHelper) {
        this.registry = registry;
        this.introspector = introspector;
        this.propertyHelper = propertyHelper;
    }


    @Init
    public void start() {
        registry.registerLoader(QNAME, this);
    }

    @Destroy
    public void stop() {
        registry.unregisterLoader(QNAME);
    }


    public ComponentDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException, LoaderException {

        String name = reader.getAttributeValue(null, "name");
        Document value = propertyHelper.loadValue(reader);
        PropertyValue propertyValue = new PropertyValue("feature", null, value);

        final Class<FeatureComponent> implClass = FeatureComponent.class;
        PojoComponentType componentType = getComponentType(implClass, context);
        componentType.setImplementationScope(Scope.COMPOSITE);

        final SystemImplementation featureImplementation = new SystemImplementation();
        featureImplementation.setImplementationClass(implClass.getName());
        featureImplementation.setComponentType(componentType);

        ComponentDefinition<Implementation<?>> def = new ComponentDefinition<Implementation<?>>(name);
        def.setImplementation(featureImplementation);
        def.add(propertyValue);

        return def;

    }

    /*
    * Loads the component type for the reflection marshaller.
    */
    private PojoComponentType getComponentType(Class<FeatureComponent> implClass, IntrospectionContext context) throws LoaderException {

        PojoComponentType componentType = new PojoComponentType(implClass.getName());
        introspector.introspect(implClass, componentType, context);

        return componentType;

    }

}
