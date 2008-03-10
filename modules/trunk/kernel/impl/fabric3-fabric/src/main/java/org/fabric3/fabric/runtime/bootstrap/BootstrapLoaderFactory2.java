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
package org.fabric3.fabric.runtime.bootstrap;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Destroy;

import org.fabric3.fabric.implementation.system.SystemComponentTypeLoader;
import org.fabric3.fabric.implementation.system.SystemComponentTypeLoaderImpl2;
import org.fabric3.fabric.implementation.system.SystemConstructorHeuristic;
import org.fabric3.fabric.implementation.system.SystemHeuristic;
import org.fabric3.fabric.implementation.system.SystemImplementation;
import org.fabric3.fabric.implementation.system.SystemImplementationLoader;
import org.fabric3.fabric.implementation.system.SystemServiceHeuristic;
import org.fabric3.fabric.implementation.system.SystemUnannotatedHeuristic;
import org.fabric3.introspection.impl.DefaultClassWalker;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.impl.annotation.PropertyProcessor;
import org.fabric3.introspection.impl.annotation.ReferenceProcessor;
import org.fabric3.introspection.impl.annotation.EagerInitProcessor;
import org.fabric3.introspection.impl.annotation.InitProcessor;
import org.fabric3.introspection.impl.annotation.DestroyProcessor;
import org.fabric3.introspection.impl.annotation.MonitorProcessor;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.java.AnnotationProcessor;
import org.fabric3.introspection.java.ClassWalker;
import org.fabric3.introspection.java.ContractProcessor;
import org.fabric3.introspection.java.IntrospectionHelper;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.loader.common.ComponentReferenceLoader;
import org.fabric3.loader.common.ComponentServiceLoader;
import org.fabric3.loader.common.PropertyLoader;
import org.fabric3.loader.composite.ComponentLoader;
import org.fabric3.loader.composite.CompositeLoader;
import org.fabric3.loader.composite.IncludeLoader;
import org.fabric3.loader.composite.PropertyValueLoader;
import org.fabric3.loader.impl.DefaultLoaderHelper;
import org.fabric3.loader.impl.LoaderRegistryImpl;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.spi.services.factories.xml.XMLFactory;
import org.fabric3.api.annotation.Monitor;

/**
 * @version $Rev$ $Date$
 */
public class BootstrapLoaderFactory2 {

    public static Loader createLoader(MonitorFactory monitorFactory, XMLFactory xmlFactory) {

        LoaderHelper loaderHelper = new DefaultLoaderHelper();

        LoaderRegistryImpl loader = new LoaderRegistryImpl(monitorFactory.getMonitor(LoaderRegistryImpl.Monitor.class), xmlFactory);
        Map<QName, TypeLoader<?>> loaders = new HashMap<QName, TypeLoader<?>>();

        // loader for <composite> document
        loaders.put(CompositeLoader.COMPOSITE, compositeLoader(loader, loaderHelper));

        // loader for <implementation.system> element
        loaders.put(SystemImplementation.IMPLEMENTATION_SYSTEM, systemImplementation());

        loader.setLoaders(loaders);
        return loader;
    }

    private static CompositeLoader compositeLoader(Loader loader, LoaderHelper loaderHelper) {
        PropertyLoader propertyLoader = new PropertyLoader(loaderHelper);
        PropertyValueLoader propertyValueLoader = new PropertyValueLoader(loaderHelper);


        ComponentReferenceLoader componentReferenceLoader = new ComponentReferenceLoader(loader, loaderHelper);
        ComponentServiceLoader componentServiceLoader = new ComponentServiceLoader(loader, loaderHelper);
        ComponentLoader componentLoader = new ComponentLoader(loader,
                                                              propertyValueLoader,
                                                              componentReferenceLoader,
                                                              componentServiceLoader,
                                                              loaderHelper);

        IncludeLoader includeLoader = new IncludeLoader(loader, null);
        return new CompositeLoader(loader, includeLoader, propertyLoader, componentLoader, loaderHelper);
    }

    private static SystemImplementationLoader systemImplementation() {
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        ContractProcessor contractProcessor = new DefaultContractProcessor(helper);

        Map<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation, SystemImplementation>> processors =
                new HashMap<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation, SystemImplementation>>();

        // no constructor processor is needed as that is handled by heuristics
        processors.put(Property.class, new PropertyProcessor<SystemImplementation>(helper));
        processors.put(Reference.class, new ReferenceProcessor<SystemImplementation>(contractProcessor, helper));
        processors.put(EagerInit.class, new EagerInitProcessor<SystemImplementation>());
        processors.put(Init.class, new InitProcessor<SystemImplementation>());
        processors.put(Destroy.class, new DestroyProcessor<SystemImplementation>());
        processors.put(Monitor.class, new MonitorProcessor<SystemImplementation>(helper, contractProcessor));

        ClassWalker<SystemImplementation> classWalker = new DefaultClassWalker<SystemImplementation>(processors);

        // heuristics for system components
        SystemServiceHeuristic serviceHeuristic = new SystemServiceHeuristic(contractProcessor, helper);
        SystemConstructorHeuristic constructorHeuristic = new SystemConstructorHeuristic();
        SystemUnannotatedHeuristic unannotatedHeuristic = new SystemUnannotatedHeuristic(helper, contractProcessor);
        SystemHeuristic systemHeuristic = new SystemHeuristic(serviceHeuristic, constructorHeuristic, unannotatedHeuristic);
        Collection<SystemHeuristic> heuristics = Collections.singletonList(systemHeuristic);

        SystemComponentTypeLoader typeLoader = new SystemComponentTypeLoaderImpl2(classWalker, heuristics, helper);
        return new SystemImplementationLoader(typeLoader);
    }
}
