/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.sandbox.introspection.impl;

import java.net.URI;

import org.fabric3.fabric.runtime.AbstractRuntime;
import org.fabric3.fabric.runtime.ComponentNames;
import org.fabric3.fabric.model.logical.LogicalModelGenerator;
import org.fabric3.introspection.java.ImplementationProcessor;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.sandbox.introspection.IntrospectionHostInfo;
import org.fabric3.sandbox.introspection.IntrospectionRuntime;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Composite;
import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.java.introspection.JavaImplementationProcessor;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.spi.runtime.assembly.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * @version $Rev$ $Date$
 */
public class IntrospectionRuntimeImpl extends AbstractRuntime<IntrospectionHostInfo> implements IntrospectionRuntime {
    private static final URI LOADER_URI = URI.create(ComponentNames.RUNTIME_NAME + "/loader");
    private static final URI JAVA_PROCESSOR = URI.create(ComponentNames.RUNTIME_NAME + "/org.fabric3.java.introspection.JavaImplementationProcessorImpl");
    private static final URI LOGICAL_COMPONENT_MANAGER = URI.create(ComponentNames.RUNTIME_NAME + "/logicalComponentManager");
    private static final URI LOGICAL_MODEL_GENERATOR = URI.create(ComponentNames.RUNTIME_NAME + "/logicalModelGenerator");

    public IntrospectionRuntimeImpl(MonitorFactory monitorFactory) {
        super(IntrospectionHostInfo.class, monitorFactory);
    }

    public Loader getLoader() {
        return getSystemComponent(Loader.class, LOADER_URI);
    }

    public <I extends Implementation<?>, IP extends ImplementationProcessor<I>> IP getImplementationProcessor(Class<I> implementationType) {
        if (JavaImplementation.class.equals(implementationType)) {
            return (IP) getSystemComponent(JavaImplementationProcessor.class, JAVA_PROCESSOR);
        }
        return null;
    }

    public void initializeContext(Composite context) throws ActivateException {
        LogicalComponentManager componentManager = getSystemComponent(LogicalComponentManager.class, LOGICAL_COMPONENT_MANAGER);
        try {
            componentManager.initialize();
        } catch (AssemblyException e) {
            throw new ActivateException(e);
        }
    }

    public void validate(Composite include) throws ActivateException {
        LogicalComponentManager componentManager = getSystemComponent(LogicalComponentManager.class, LOGICAL_COMPONENT_MANAGER);
        LogicalModelGenerator generator = getSystemComponent(LogicalModelGenerator.class, LOGICAL_MODEL_GENERATOR);
        LogicalCompositeComponent domain = componentManager.getDomain();
        generator.include(domain, include);
    }
}
