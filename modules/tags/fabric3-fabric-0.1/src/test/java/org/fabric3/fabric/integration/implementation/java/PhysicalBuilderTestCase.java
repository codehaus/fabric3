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
package org.fabric3.fabric.integration.implementation.java;

import java.lang.annotation.ElementType;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.fabric.builder.Connector;
import org.fabric3.fabric.builder.ConnectorImpl;
import org.fabric3.fabric.builder.component.DefaultComponentBuilderRegistry;
import org.fabric3.fabric.builder.component.WireAttacherRegistryImpl;
import org.fabric3.fabric.component.ComponentManagerImpl;
import org.fabric3.fabric.component.scope.CompositeScopeContainer;
import org.fabric3.fabric.deployer.DeployerImpl;
import org.fabric3.fabric.deployer.DeployerMonitor;
import org.fabric3.fabric.implementation.java.JavaComponentBuilder;
import org.fabric3.fabric.implementation.java.JavaComponentDefinition;
import org.fabric3.fabric.implementation.java.JavaWireAttacher;
import org.fabric3.fabric.implementation.java.JavaWireSourceDefinition;
import org.fabric3.fabric.implementation.java.JavaWireTargetDefinition;
import org.fabric3.fabric.services.classloading.ClassLoaderRegistryImpl;
import org.fabric3.fabric.services.instancefactory.BuildHelperImpl;
import org.fabric3.fabric.services.instancefactory.DefaultInstanceFactoryBuilderRegistry;
import org.fabric3.fabric.services.instancefactory.ReflectiveInstanceFactoryBuilder;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.pojo.instancefactory.InjectionSiteMapping;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.pojo.instancefactory.MemberSite;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.model.instance.ValueSource;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;

/**
 * @version $Rev$ $Date$
 */
public class PhysicalBuilderTestCase extends TestCase {
    private URI groupId;
    private URI sourceId;
    private URI targetId;
    private ClassLoaderRegistry classLoaderRegistry;
    private JavaComponentBuilder builder;
    private ScopeContainer<URI> scopeContainer;
    private ScopeRegistry scopeRegistry;
    private Connector connector;
    private ComponentManager componentManager;
    private DeployerImpl deployer;
    private PhysicalChangeSet pcs;
    private WorkContext workContext;

    public void testWireTwoComponents() throws Exception {
        pcs.addComponentDefinition(createSourceComponentDefinition());
        pcs.addComponentDefinition(createTargetComponentDefinition());
        pcs.addWireDefinition(createOptimizedWire());
        deployer.applyChangeSet(pcs);

        PojoWorkContextTunnel.setThreadWorkContext(workContext);
        try {
            AtomicComponent<?> sourceComponent = (AtomicComponent<?>) componentManager.getComponent(sourceId);
            InstanceWrapper<?> wrapper = scopeContainer.getWrapper(sourceComponent, workContext);
            SourceImpl s = (SourceImpl) wrapper.getInstance();
            assertSame(s.target.getClass(), TargetImpl.class);
        } finally {
            PojoWorkContextTunnel.setThreadWorkContext(null);
        }
    }

    private JavaComponentDefinition createSourceComponentDefinition() {
        InstanceFactoryDefinition sourceProviderDefinition = new InstanceFactoryDefinition();
        sourceProviderDefinition.setImplementationClass(SourceImpl.class.getName());
        InjectionSiteMapping mapping = new InjectionSiteMapping();
        mapping.setSource(new ValueSource(ValueSource.ValueSourceType.REFERENCE, "target"));
        mapping.setSite(new MemberSite(ElementType.FIELD, "target"));
        sourceProviderDefinition.addInjectionSite(mapping);

        JavaComponentDefinition source = new JavaComponentDefinition();
        source.setComponentId(sourceId);
        source.setGroupId(groupId);
        source.setClassLoaderId(groupId);
        source.setScope(Scope.COMPOSITE);
        source.setInstanceFactoryProviderDefinition(sourceProviderDefinition);
        return source;
    }

    private JavaComponentDefinition createTargetComponentDefinition() {
        InstanceFactoryDefinition targetProviderDefinition = new InstanceFactoryDefinition();
        targetProviderDefinition.setImplementationClass(TargetImpl.class.getName());

        JavaComponentDefinition target = new JavaComponentDefinition();
        target.setComponentId(targetId);
        target.setGroupId(groupId);
        target.setClassLoaderId(groupId);
        target.setScope(Scope.COMPOSITE);
        target.setInstanceFactoryProviderDefinition(targetProviderDefinition);
        return target;
    }

    private PhysicalWireDefinition createOptimizedWire() {
        JavaWireSourceDefinition wireSource = new JavaWireSourceDefinition();
        wireSource.setUri(sourceId.resolve("#target"));
        wireSource.setOptimizable(true);
        JavaWireTargetDefinition wireTarget = new JavaWireTargetDefinition();
        wireTarget.setUri(targetId);
        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition();
        wireDefinition.setSource(wireSource);
        wireDefinition.setTarget(wireTarget);
        return wireDefinition;
    }

    protected void setUp() throws Exception {
        super.setUp();
        groupId = URI.create("sca://./composite");
        sourceId = groupId.resolve("composite/source");
        targetId = groupId.resolve("composite/target");
        classLoaderRegistry = new ClassLoaderRegistryImpl();
        ClassLoader classLoader = getClass().getClassLoader();
        classLoaderRegistry.register(groupId, classLoader);

        workContext = new SimpleWorkContext();
        workContext.setScopeIdentifier(Scope.COMPOSITE, groupId);

        MonitorFactory monitorFactory = EasyMock.createNiceMock(MonitorFactory.class);
        scopeContainer = new CompositeScopeContainer(monitorFactory);
        scopeContainer.start();
        scopeContainer.startContext(workContext, groupId);

        scopeRegistry = EasyMock.createMock(ScopeRegistry.class);
        EasyMock.expect(scopeRegistry.getScopeContainer(Scope.COMPOSITE)).andStubReturn(scopeContainer);
        EasyMock.replay(scopeRegistry);

        InstanceFactoryBuilderRegistry providerBuilders = new DefaultInstanceFactoryBuilderRegistry();
        InstanceFactoryBuildHelper buildHelper = new BuildHelperImpl(classLoaderRegistry);
        ReflectiveInstanceFactoryBuilder instanceFactoryBuilder = new ReflectiveInstanceFactoryBuilder(providerBuilders, buildHelper);
        instanceFactoryBuilder.init();

        DefaultComponentBuilderRegistry builderRegistry = new DefaultComponentBuilderRegistry();
        componentManager = new ComponentManagerImpl();
        builder = new JavaComponentBuilder(builderRegistry,
                                           componentManager,
                                           scopeRegistry,
                                           providerBuilders,
                                           classLoaderRegistry,
                                           null, null);
        builder.init();

        WireAttacherRegistry wireAttacherRegistry = new WireAttacherRegistryImpl();
        JavaWireAttacher wireAttacher = new JavaWireAttacher(componentManager, wireAttacherRegistry, null, classLoaderRegistry);
        wireAttacher.init();
        connector = new ConnectorImpl(null, wireAttacherRegistry);

        DeployerMonitor monitor = EasyMock.createNiceMock(DeployerMonitor.class);
        EasyMock.replay(monitor);
        deployer = new DeployerImpl(monitor);
        deployer.setBuilderRegistry(builderRegistry);
        deployer.setComponentManager(componentManager);
        deployer.setConnector(connector);

        pcs = new PhysicalChangeSet();
    }

    public static class SourceImpl {
        public TargetImpl target;
    }

    public static class TargetImpl {
    }
}
