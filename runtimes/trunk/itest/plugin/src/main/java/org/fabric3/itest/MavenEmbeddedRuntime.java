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
package org.fabric3.itest;

import java.lang.reflect.Method;
import java.net.URI;

import org.apache.maven.surefire.testset.TestSetFailedException;

import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.fabric.assembly.DistributedAssembly;
import org.fabric3.fabric.implementation.java.JavaComponent;
import org.fabric3.fabric.implementation.java.JavaInvokerInterceptor;
import org.fabric3.fabric.implementation.pojo.PojoWorkContextTunnel;
import org.fabric3.fabric.runtime.AbstractRuntime;
import static org.fabric3.fabric.runtime.ComponentNames.CLASSLOADER_REGISTRY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.DISTRIBUTED_ASSEMBLY_URI;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.model.type.Operation;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;

/**
 * @version $Rev$ $Date$
 */
public class MavenEmbeddedRuntime extends AbstractRuntime<MavenHostInfo> {
    public MavenEmbeddedRuntime() {
        super(MavenHostInfo.class);
    }

    public void deploy(ComponentDefinition<CompositeImplementation> definition)
            throws Exception {
        ClassLoader testClassLoader = definition.getImplementation().getClassLoader();
        // FIXME JFM this is a horrible hack until the contribution service is in place
        ClassLoaderRegistry classLoaderRegistry =
                getSystemComponent(ClassLoaderRegistry.class, CLASSLOADER_REGISTRY_URI);
        classLoaderRegistry.register(URI.create("sca://./applicationClassLoader"), testClassLoader);
        DistributedAssembly assembly = getSystemComponent(DistributedAssembly.class, DISTRIBUTED_ASSEMBLY_URI);
        // deploy the components
        assembly.activate(definition, true);
    }

    public void startContext(URI compositeId) throws GroupInitializationException {
        WorkContext workContext = new SimpleWorkContext();
        workContext.setScopeIdentifier(Scope.COMPOSITE, compositeId);
        getScopeRegistry().getScopeContainer(Scope.COMPOSITE).startContext(workContext,
                                                                           URI.create(compositeId.toString() + "/"));
    }

    @SuppressWarnings({"unchecked"})
    public void executeTest(URI contextId, String componentName, Operation<?> operation) throws TestSetFailedException {
        WorkContext oldContext = PojoWorkContextTunnel.getThreadWorkContext();
        try {
            WorkContext workContext = new SimpleWorkContext();
            workContext.setScopeIdentifier(Scope.COMPOSITE, contextId);
            URI componentId = URI.create(contextId.toString() + "/" + componentName);

            // FIXME we should not be creating a JavaInvokerInterceptor here
            // FIXME this should create a wire to the JUnit component and invoke the head interceptor on the chain
            JavaComponent component = (JavaComponent) getComponentManager().getComponent(componentId);
            PojoWorkContextTunnel.setThreadWorkContext(workContext);
            Object instance = component.createObjectFactory().getInstance();
            Method m = instance.getClass().getMethod(operation.getName());
            ScopeContainer scopeContainer = component.getScopeContainer();
            JavaInvokerInterceptor<?, ?> interceptor = new JavaInvokerInterceptor(m, component, scopeContainer);

            Message msg = new MessageImpl();
            msg.setWorkContext(workContext);
            Message response = interceptor.invoke(msg);
            if (response.isFault()) {
                throw new TestSetFailedException(operation.getName(), (Throwable) response.getBody());
            }
        } catch (NoSuchMethodException e) {
            throw new AssertionError();
        } catch (ObjectCreationException e) {
            throw new AssertionError();
        } finally {
            PojoWorkContextTunnel.setThreadWorkContext(oldContext);
        }
    }
}
