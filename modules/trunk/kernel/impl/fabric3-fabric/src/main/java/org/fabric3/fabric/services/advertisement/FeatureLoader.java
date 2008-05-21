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
package org.fabric3.fabric.services.advertisement;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.PropertyValue;
import org.fabric3.system.introspection.SystemImplementationProcessor;
import org.fabric3.system.scdl.SystemImplementation;

/**
 * @version $Revision: 1 $ $Date: 2007-05-14 18:40:37 +0100 (Mon, 14 May 2007) $
 */
@EagerInit
public class FeatureLoader implements TypeLoader<ComponentDefinition> {

    // Qualified name of the root element.
    //private static final QName QNAME = new QName(Constants.FABRIC3_SYSTEM_NS, "feature");

    private final SystemImplementation featureImplementation;

    private final LoaderHelper helper;

    public FeatureLoader(@Reference SystemImplementationProcessor processor, @Reference LoaderHelper helper) {
        this.helper = helper;

        featureImplementation = new SystemImplementation(FeatureComponent.class.getName());
        IntrospectionContext context = new DefaultIntrospectionContext(getClass().getClassLoader(), null, null);
        processor.introspect(featureImplementation, context);
    }

    public ComponentDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException, LoaderException {

        String name = reader.getAttributeValue(null, "name");
        Document value = helper.loadValue(reader);
        PropertyValue propertyValue = new PropertyValue("feature", null, value);

        ComponentDefinition<Implementation<?>> def = new ComponentDefinition<Implementation<?>>(name);
        def.setImplementation(featureImplementation);
        def.add(propertyValue);

        return def;

    }
}
