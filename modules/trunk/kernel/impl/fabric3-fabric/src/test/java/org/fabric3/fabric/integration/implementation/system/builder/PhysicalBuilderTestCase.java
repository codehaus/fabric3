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
package org.fabric3.fabric.integration.implementation.system.builder;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.fabric.builder.ConnectorImpl;
import org.fabric3.fabric.builder.component.DefaultComponentBuilderRegistry;
import org.fabric3.fabric.component.scope.CompositeScopeContainer;
import org.fabric3.fabric.component.scope.ScopeContainerMonitor;
import org.fabric3.fabric.deployer.DeployerImpl;
import org.fabric3.fabric.deployer.DeployerMonitor;
import org.fabric3.fabric.implementation.system.SystemComponentBuilder;
import org.fabric3.fabric.implementation.system.SystemComponentDefinition;
import org.fabric3.fabric.implementation.system.SystemWireAttacher;
import org.fabric3.fabric.implementation.system.SystemWireSourceDefinition;
import org.fabric3.fabric.implementation.system.SystemWireTargetDefinition;
import org.fabric3.fabric.services.classloading.ClassLoaderRegistryImpl;
import org.fabric3.fabric.services.componentmanager.ComponentManagerImpl;
import org.fabric3.fabric.services.instancefactory.BuildHelperImpl;
import org.fabric3.fabric.services.instancefactory.DefaultInstanceFactoryBuilderRegistry;
import org.fabric3.fabric.services.instancefactory.ReflectiveInstanceFactoryBuilder;
import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.Scope;
import org.fabric3.scdl.Signature;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.transform.DefaultTransformerRegistry;

/**
 * @version $Rev$ $Date$
 */
public class PhysicalBuilderTestCase extends TestCase {
    private URI groupId;
    private URI sourceId;
    private URI targetId;
    private ClassLoaderRegistry classLoaderRegistry;
    private SystemComponentBuilder builder;
    private ScopeContainer<URI> scopeContainer;
    private ScopeRegistry scopeRegistry;
    private ConnectorImpl connector;
    private ComponentManager componentManager;
    private DeployerImpl deployer;
    private PhysicalChangeSet pcs;
    private WorkContext workContext;

    public void testWireTwoComponents() throws Exception {
        pcs.addComponentDefinition(createSourceComponentDefinition());
        pcs.addComponentDefinition(createTargetComponentDefinition());
        pcs.addWireDefinition(createWireDefinition());
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

    private SystemComponentDefinition createSourceComponentDefinition() throws Exception {
        InstanceFactoryDefinition sourceProviderDefinition = new InstanceFactoryDefinition();
        sourceProviderDefinition.setImplementationClass(SourceImpl.class.getName());
        sourceProviderDefinition.setConstructor(new Signature(SourceImpl.class.getConstructor()));
        sourceProviderDefinition.addInjectionSite(new InjectableAttribute(InjectableAttributeType.REFERENCE, "target"),
                                                  new FieldInjectionSite(SourceImpl.class.getField("target")));

        SystemComponentDefinition source = new SystemComponentDefinition();
        source.setComponentId(sourceId);
        source.setGroupId(groupId);
        source.setClassLoaderId(groupId);
        source.setScope(Scope.COMPOSITE);
        source.setInstanceFactoryProviderDefinition(sourceProviderDefinition);
        return source;
    }

    private SystemComponentDefinition createTargetComponentDefinition() throws Exception {
        InstanceFactoryDefinition targetProviderDefinition = new InstanceFactoryDefinition();
        targetProviderDefinition.setImplementationClass(TargetImpl.class.getName());
        targetProviderDefinition.setConstructor(new Signature(TargetImpl.class.getConstructor()));

        SystemComponentDefinition target = new SystemComponentDefinition();
        target.setComponentId(targetId);
        target.setGroupId(groupId);
        target.setClassLoaderId(groupId);
        target.setScope(Scope.COMPOSITE);
        target.setInstanceFactoryProviderDefinition(targetProviderDefinition);
        return target;
    }

    private PhysicalWireDefinition createWireDefinition() {
        SystemWireSourceDefinition wireSource = new SystemWireSourceDefinition();
        wireSource.setUri(sourceId.resolve("#target"));
        wireSource.setValueSource(new InjectableAttribute(InjectableAttributeType.REFERENCE, "target"));
        SystemWireTargetDefinition wireTarget = new SystemWireTargetDefinition();
        wireTarget.setUri(targetId);
        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition(wireSource, wireTarget);
        wireDefinition.setOptimizable(true);
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

        scopeContainer = new CompositeScopeContainer(EasyMock.createNiceMock(ScopeContainerMonitor.class));
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
        builder = new SystemComponentBuilder(builderRegistry,
                                             scopeRegistry,
                                             providerBuilders,
                                             classLoaderRegistry,
                                             null);
        builder.init();

        connector = new ConnectorImpl(null);
        SystemWireAttacher wireAttacher =
                new SystemWireAttacher(componentManager, connector, connector, new DefaultTransformerRegistry(), new ClassLoaderRegistryImpl());
        wireAttacher.init();
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
