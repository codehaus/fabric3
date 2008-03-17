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

import org.fabric3.fabric.runtime.ComponentNames;
import org.fabric3.fabric.runtime.bootstrap.ScdlBootstrapperImpl;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.introspection.java.ImplementationProcessor;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.monitor.impl.NullMonitorFactory;
import org.fabric3.sandbox.introspection.IntrospectionFactory;
import org.fabric3.sandbox.introspection.IntrospectionRuntime;
import org.fabric3.scdl.Implementation;
import org.fabric3.services.xmlfactory.XMLFactory;
import org.fabric3.services.xmlfactory.impl.DefaultXMLFactoryImpl;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;

/**
 * @version $Rev$ $Date$
 */
public class IntrospectionFactoryImpl implements IntrospectionFactory {
    private final IntrospectionRuntime runtime;

    /**
     * Constructor specifying the monitor and xml factories to use.
     *
     * @param monitorFactory the factory for monitors
     * @param xmlFactory     the factory for XML parsers
     * @throws InitializationException      if there was a problem initializing the runtime
     * @throws GroupInitializationException if there was a problem starting the runtime
     */
    public IntrospectionFactoryImpl(MonitorFactory monitorFactory, XMLFactory xmlFactory)
            throws InitializationException, GroupInitializationException {
        ClassLoader classLoader = getClass().getClassLoader();

        runtime = new IntrospectionRuntimeImpl(monitorFactory, xmlFactory);
        runtime.setHostInfo(new IntrospectionHostInfoImpl());
        runtime.setHostClassLoader(classLoader);
        runtime.initialize();
        ScopeContainer<?> container = runtime.getScopeContainer();

        ScdlBootstrapperImpl bootstrapper = new ScdlBootstrapperImpl();
        bootstrapper.setXmlFactory(xmlFactory);
        bootstrapper.setScdlLocation(getClass().getResource("introspection.composite"));
        bootstrapper.bootPrimordial(runtime, classLoader, classLoader);

        WorkContext workContext = new WorkContext();
        workContext.addCallFrame(new CallFrame());
        container.startContext(workContext, ComponentNames.RUNTIME_URI);

        bootstrapper.bootSystem(runtime);
    }

    /**
     * Default constructor which will use a non-logging monitor and the default StAX implementation.
     *
     * @throws InitializationException      if there was a problem initializing the runtime
     * @throws GroupInitializationException if there was a problem starting the runtime
     */
    public IntrospectionFactoryImpl() throws InitializationException, GroupInitializationException {
        this(new NullMonitorFactory(), new DefaultXMLFactoryImpl());
    }

    public Loader getLoader() {
        return runtime.getLoader();
    }

    public <I extends Implementation<?>, IP extends ImplementationProcessor<I>> IP getImplementationProcessor(Class<I> implementationType) {
        return (IP) runtime.getImplementationProcessor(implementationType);
    }
}
