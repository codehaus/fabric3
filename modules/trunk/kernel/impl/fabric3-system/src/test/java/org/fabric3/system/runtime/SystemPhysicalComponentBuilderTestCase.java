/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.system.runtime;

import java.net.URI;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.model.type.java.Signature;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.instancefactory.InstanceFactoryProvider;
import org.fabric3.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.introspection.IntrospectionHelper;
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
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        URI classLoaderId = URI.create("classLoaderId");
        QName deployable = new QName("test", "test");
        componentId = URI.create("fabric3://component");

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

        IntrospectionHelper helper = EasyMock.createNiceMock(IntrospectionHelper.class);
        EasyMock.replay(helper);

        builder = new SystemComponentBuilder<T>(scopeRegistry, providerBuilders, classLoaderRegistry, null, helper);
        definition = new SystemComponentDefinition();
        definition.setDeployable(deployable);
        definition.setComponentUri(componentId);
        definition.setClassLoaderId(classLoaderId);
        definition.setScope("COMPOSITE");
        definition.setProviderDefinition(providerDefinition);
    }
}
