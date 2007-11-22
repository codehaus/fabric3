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
import org.fabric3.fabric.runtime.AbstractRuntime;
import org.fabric3.fabric.runtime.ComponentNames;
import static org.fabric3.fabric.runtime.ComponentNames.DISTRIBUTED_ASSEMBLY_URI;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.java.JavaComponent;
import org.fabric3.maven.runtime.MavenEmbeddedRuntime;
import org.fabric3.maven.runtime.MavenHostInfo;
import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.pojo.reflection.InvokerInterceptor;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;

/**
 * @version $Rev: 1382 $ $Date: 2007-09-23 21:08:40 +0100 (Sun, 23 Sep 2007) $
 */
public class MavenEmbeddedRuntimeImpl extends AbstractRuntime<MavenHostInfo> implements MavenEmbeddedRuntime {
    public MavenEmbeddedRuntimeImpl(MonitorFactory monitorFactory) {
        super(MavenHostInfo.class, monitorFactory);
    }

    public void deploy(Composite composite) throws Exception {
        DistributedAssembly assembly = getSystemComponent(DistributedAssembly.class, DISTRIBUTED_ASSEMBLY_URI);
        assembly.includeInDomain(composite);
    }

    public void startContext(URI compositeId) throws GroupInitializationException {
        WorkContext workContext = new SimpleWorkContext();
        workContext.setScopeIdentifier(Scope.COMPOSITE, compositeId);
        getScopeRegistry().getScopeContainer(Scope.COMPOSITE).startContext(workContext,
                                                                           URI.create(compositeId.toString() + "/"));
    }

    public void destroy() {
        // destroy system components
        ScopeRegistry scopeRegistry = getScopeRegistry();
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(Scope.COMPOSITE);
        WorkContext workContext = new SimpleWorkContext();
        URI systemGroupId = URI.create(ComponentNames.RUNTIME_NAME + "/");
        workContext.setScopeIdentifier(Scope.COMPOSITE, systemGroupId);
        scopeContainer.stopContext(workContext);
    }

    @SuppressWarnings({"unchecked"})
    public void executeTest(URI contextId, String componentName, Operation<?> operation) throws TestSetFailedException {
        WorkContext oldContext = PojoWorkContextTunnel.getThreadWorkContext();
        try {
            WorkContext workContext = new SimpleWorkContext();
            workContext.setScopeIdentifier(Scope.COMPOSITE, contextId);
            URI componentId = URI.create(contextId.toString() + "/" + componentName);

            // FIXME we should not be creating a InvokerInterceptor here
            // FIXME this should create a wire to the JUnit component and invoke the head interceptor on the chain
            JavaComponent component = (JavaComponent) getComponentManager().getComponent(componentId);
            PojoWorkContextTunnel.setThreadWorkContext(workContext);
            Object instance = component.createObjectFactory().getInstance();
            Method m = instance.getClass().getMethod(operation.getName());
            ScopeContainer scopeContainer = component.getScopeContainer();
            InvokerInterceptor<?, ?> interceptor = new InvokerInterceptor(m, component, scopeContainer);

            Message msg = new MessageImpl();
            msg.setWorkContext(workContext);
            Message response = interceptor.invoke(msg);
            if (response.isFault()) {
                throw new TestSetFailedException(operation.getName(), (Throwable) response.getBody());
            }
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        } catch (ObjectCreationException e) {
            throw new AssertionError(e);
        } finally {
            PojoWorkContextTunnel.setThreadWorkContext(oldContext);
        }
    }
}
