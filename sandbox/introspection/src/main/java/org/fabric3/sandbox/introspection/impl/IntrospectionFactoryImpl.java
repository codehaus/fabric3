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

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import org.fabric3.fabric.implementation.IntrospectionRegistryImpl;
import org.fabric3.fabric.implementation.processor.ConstructorProcessor;
import org.fabric3.fabric.implementation.processor.DestroyProcessor;
import org.fabric3.fabric.implementation.processor.EagerInitProcessor;
import org.fabric3.fabric.implementation.processor.HeuristicPojoProcessor;
import org.fabric3.fabric.implementation.processor.ImplementationProcessorServiceImpl;
import org.fabric3.fabric.implementation.processor.InitProcessor;
import org.fabric3.fabric.implementation.processor.PostConstructProcessor;
import org.fabric3.fabric.implementation.processor.PreDestroyProcessor;
import org.fabric3.fabric.implementation.processor.PropertyProcessor;
import org.fabric3.fabric.implementation.processor.ReferenceProcessor;
import org.fabric3.fabric.implementation.processor.ServiceProcessor;
import org.fabric3.loader.impl.LoaderRegistryImpl;
import org.fabric3.services.xmlfactory.impl.XMLFactoryImpl;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.java.ContractProcessor;
import org.fabric3.introspection.java.IntrospectionHelper;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.java.JavaComponentTypeLoader;
import org.fabric3.java.JavaComponentTypeLoaderImpl;
import org.fabric3.java.JavaImplementation;
import org.fabric3.java.JavaImplementationLoader;
import org.fabric3.loader.common.ComponentReferenceLoader;
import org.fabric3.loader.common.ComponentServiceLoader;
import org.fabric3.loader.impl.DefaultLoaderHelper;
import org.fabric3.loader.common.PropertyLoader;
import org.fabric3.loader.composite.ComponentLoader;
import org.fabric3.loader.composite.CompositeLoader;
import org.fabric3.loader.composite.CompositeReferenceLoader;
import org.fabric3.loader.composite.CompositeServiceLoader;
import org.fabric3.loader.composite.IncludeLoader;
import org.fabric3.loader.composite.PropertyValueLoader;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.monitor.impl.NullMonitorFactory;
import org.fabric3.pojo.processor.ImplementationProcessorService;
import org.fabric3.pojo.processor.Introspector;
import org.fabric3.sandbox.introspection.IntrospectionFactory;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.services.xmlfactory.XMLFactory;

/**
 * @version $Rev$ $Date$
 */
public class IntrospectionFactoryImpl implements IntrospectionFactory {
    private static final MonitorFactory monitorFactory = new NullMonitorFactory();

    private Loader loader;

    public IntrospectionFactoryImpl() {
        loader = createLoader(monitorFactory, null);
    }

    public Loader getLoader() {
        return loader;
    }

    public static Loader createLoader(MonitorFactory monitorFactory,
                                      MetaDataStore metaDataStore) {

        XMLFactory xmlFactory = new XMLFactoryImpl();
        LoaderRegistryImpl loader = new LoaderRegistryImpl(monitorFactory.getMonitor(LoaderRegistryImpl.Monitor.class), xmlFactory);

        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        LoaderHelper loaderHelper = new DefaultLoaderHelper();

        Introspector introspector = createIntrospector(monitorFactory, helper);

        PropertyLoader propertyLoader = new PropertyLoader(loaderHelper);
        PropertyValueLoader propertyValueLoader = new PropertyValueLoader(loaderHelper);

        CompositeServiceLoader serviceLoader = new CompositeServiceLoader(loader, loaderHelper);
        CompositeReferenceLoader referenceLoader = new CompositeReferenceLoader(loader, loaderHelper);


        ComponentReferenceLoader componentReferenceLoader = new ComponentReferenceLoader(loader, loaderHelper);
        ComponentServiceLoader componentServiceLoader = new ComponentServiceLoader(loader, loaderHelper);
        ComponentLoader componentLoader = new ComponentLoader(loader,
                                                              propertyValueLoader,
                                                              componentReferenceLoader,
                                                              componentServiceLoader,
                                                              loaderHelper);

        IncludeLoader includeLoader = new IncludeLoader(loader, metaDataStore);
        CompositeLoader compositeLoader = new CompositeLoader(loader,
                                                              includeLoader,
                                                              propertyLoader,
                                                              serviceLoader,
                                                              referenceLoader,
                                                              componentLoader,
                                                              null,
                                                              loaderHelper);
        compositeLoader.init();

        Map<QName, TypeLoader<?>> mappedLoaders = new HashMap<QName, TypeLoader<?>>();
        loader.setLoaders(mappedLoaders);

        JavaComponentTypeLoader javaComponentTypeLoader = new JavaComponentTypeLoaderImpl(introspector, helper);
        JavaImplementationLoader javaLoader = new JavaImplementationLoader(javaComponentTypeLoader, loaderHelper);
        mappedLoaders.put(JavaImplementation.IMPLEMENTATION_JAVA, javaLoader);
        return loader;
    }

    private static Introspector createIntrospector(MonitorFactory monitorFactory, IntrospectionHelper helper) {

        ContractProcessor contractProcessor = new DefaultContractProcessor(helper);
        ImplementationProcessorService service = new ImplementationProcessorServiceImpl(contractProcessor, helper);

        IntrospectionRegistryImpl.Monitor monitor = monitorFactory.getMonitor(IntrospectionRegistryImpl.Monitor.class);
        IntrospectionRegistryImpl introspectionRegistry = new IntrospectionRegistryImpl(monitor, helper);
        introspectionRegistry.registerProcessor(new ConstructorProcessor(service));
        introspectionRegistry.registerProcessor(new DestroyProcessor());
        introspectionRegistry.registerProcessor(new InitProcessor());
        introspectionRegistry.registerProcessor(new PreDestroyProcessor());
        introspectionRegistry.registerProcessor(new PostConstructProcessor());
        introspectionRegistry.registerProcessor(new EagerInitProcessor());
        introspectionRegistry.registerProcessor(new PropertyProcessor(helper));
        introspectionRegistry.registerProcessor(new ReferenceProcessor(helper, contractProcessor));
        introspectionRegistry.registerProcessor(new ServiceProcessor(service));
        introspectionRegistry.registerProcessor(new HeuristicPojoProcessor(service));

        return introspectionRegistry;

    }
}
