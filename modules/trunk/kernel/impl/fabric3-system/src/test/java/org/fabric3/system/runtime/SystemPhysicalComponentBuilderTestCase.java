/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.system.runtime;

import java.net.URI;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.scdl.Signature;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.system.provision.SystemComponentDefinition;

/**
 * @version $Rev$ $Date$
 */
public class SystemPhysicalComponentBuilderTestCase<T> extends TestCase {
    private SystemComponentBuilder<T> builder;
    private SystemComponentDefinition definition;
    private URI componentId;

    public void testBuildSimplePOJO() throws Exception {
        SystemComponent<T> component = builder.build(definition);
        assertEquals(componentId, component.getUri());
        assertEquals(-1, component.getInitLevel());
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        URI classLoaderId = URI.create("classLoaderId");
        QName groupId = new QName("test", "test");
        componentId = URI.create("fabric3://component");

        ComponentBuilderRegistry builderRegistry = EasyMock.createMock(ComponentBuilderRegistry.class);
        ScopeRegistry scopeRegistry = EasyMock.createMock(ScopeRegistry.class);

        ClassLoader classLoader = getClass().getClassLoader();
        ClassLoaderRegistry classLoaderRegistry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(classLoaderRegistry.getClassLoader(classLoaderId)).andStubReturn(classLoader);
        EasyMock.replay(classLoaderRegistry);

        InstanceFactoryBuilderRegistry providerBuilders = EasyMock.createMock(InstanceFactoryBuilderRegistry.class);
        InstanceFactoryDefinition providerDefinition = new InstanceFactoryDefinition();
        providerDefinition.setConstructor(new Signature("Foo"));
        InstanceFactoryProvider<T> instanceFactoryProvider = EasyMock.createMock(InstanceFactoryProvider.class);
        EasyMock.expect(providerBuilders.build(providerDefinition, classLoader)).andStubReturn(instanceFactoryProvider);
        EasyMock.replay(providerBuilders);

        builder = new SystemComponentBuilder<T>(builderRegistry,
                                                scopeRegistry,
                                                providerBuilders,
                                                classLoaderRegistry,
                                                null);

        definition = new SystemComponentDefinition();
        definition.setGroupId(groupId);
        definition.setComponentId(componentId);
        definition.setClassLoaderId(classLoaderId);
        definition.setScope("COMPOSITE");
        definition.setInitLevel(-1);
        definition.setProviderDefinition(providerDefinition);
    }
}
