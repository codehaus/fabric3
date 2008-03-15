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
import java.lang.annotation.Annotation;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.ConversationID;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Remotable;
import org.osoa.sca.annotations.Service;
import org.osoa.sca.annotations.Scope;

import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.impl.DefaultClassWalker;
import org.fabric3.introspection.impl.annotation.CallbackProcessor;
import org.fabric3.introspection.impl.annotation.ServiceProcessor;
import org.fabric3.introspection.impl.annotation.PropertyProcessor;
import org.fabric3.introspection.impl.annotation.RemotableProcessor;
import org.fabric3.introspection.impl.annotation.ReferenceProcessor;
import org.fabric3.introspection.impl.annotation.EagerInitProcessor;
import org.fabric3.introspection.impl.annotation.DestroyProcessor;
import org.fabric3.introspection.impl.annotation.ScopeProcessor;
import org.fabric3.introspection.impl.annotation.InitProcessor;
import org.fabric3.introspection.impl.annotation.ConversationIDProcessor;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.java.ImplementationProcessor;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.java.ClassWalker;
import org.fabric3.introspection.java.AnnotationProcessor;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.java.introspection.JavaImplementationLoader;
import org.fabric3.java.introspection.JavaImplementationProcessor;
import org.fabric3.java.introspection.JavaImplementationProcessorImpl;
import org.fabric3.java.introspection.JavaHeuristic;
import org.fabric3.java.introspection.JavaServiceHeuristic;
import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.loader.common.ComponentReferenceLoader;
import org.fabric3.loader.common.ComponentServiceLoader;
import org.fabric3.loader.common.PropertyLoader;
import org.fabric3.loader.composite.ComponentLoader;
import org.fabric3.loader.composite.CompositeLoader;
import org.fabric3.loader.composite.CompositeReferenceLoader;
import org.fabric3.loader.composite.CompositeServiceLoader;
import org.fabric3.loader.composite.IncludeLoader;
import org.fabric3.loader.composite.PropertyValueLoader;
import org.fabric3.loader.impl.DefaultLoaderHelper;
import org.fabric3.loader.impl.LoaderRegistryImpl;
import org.fabric3.monitor.MonitorFactory;
import org.fabric3.monitor.impl.NullMonitorFactory;
import org.fabric3.sandbox.introspection.IntrospectionFactory;
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Implementation;
import org.fabric3.services.xmlfactory.XMLFactory;
import org.fabric3.services.xmlfactory.impl.DefaultXMLFactoryImpl;

/**
 * @version $Rev$ $Date$
 */
public class IntrospectionFactoryImpl implements IntrospectionFactory {
    private final Loader loader;
    private final JavaImplementationProcessor processor;

    /**
     * Constructor specifying the monitor and xml factories to use.
     *
     * @param monitorFactory the factory for monitors
     * @param xmlFactory the factory for XML parsers
     */
    public IntrospectionFactoryImpl(MonitorFactory monitorFactory, XMLFactory xmlFactory) {
        processor = createProcessor();
        loader = createLoader(monitorFactory, xmlFactory);
    }

    /**
     * Default constructor which will use a non-logging monitor and the default StAX implementation.
     */
    public IntrospectionFactoryImpl() {
        this(new NullMonitorFactory(), new DefaultXMLFactoryImpl());
    }

    public Loader getLoader() {
        return loader;
    }

    public <I extends Implementation<? extends AbstractComponentType<?, ?, ?, ?>>> ImplementationProcessor<I> getImplementationProcessor(Class<I> implementationType) {
        if (!JavaImplementation.class.equals(implementationType)) {
            throw new UnsupportedOperationException();
        }
        return (ImplementationProcessor<I>) processor;
    }

    private JavaImplementationProcessor createProcessor() {
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        ContractProcessor contractProcessor = new DefaultContractProcessor(helper);

        Map<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation, JavaImplementation>> processors =
                new HashMap<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation, JavaImplementation>>();
        processors.put(Callback.class, new CallbackProcessor<JavaImplementation>(contractProcessor, helper));
        processors.put(ConversationID.class, new ConversationIDProcessor<JavaImplementation>());
        processors.put(Destroy.class, new DestroyProcessor<JavaImplementation>());
        processors.put(EagerInit.class, new EagerInitProcessor<JavaImplementation>());
        processors.put(Init.class, new InitProcessor<JavaImplementation>());
        processors.put(Property.class, new PropertyProcessor<JavaImplementation>(helper));
        processors.put(Reference.class, new ReferenceProcessor<JavaImplementation>(contractProcessor, helper));
        processors.put(Remotable.class, new RemotableProcessor<JavaImplementation>(contractProcessor));
        processors.put(Scope.class, new ScopeProcessor<JavaImplementation>());
        processors.put(Service.class, new ServiceProcessor<JavaImplementation>(contractProcessor));
        ClassWalker<JavaImplementation> classWalker = new DefaultClassWalker<JavaImplementation>(processors);
        JavaServiceHeuristic serviceHeuristic = new JavaServiceHeuristic(helper, contractProcessor);
        JavaHeuristic heuristic = new JavaHeuristic(helper, contractProcessor, serviceHeuristic);
        return new JavaImplementationProcessorImpl(classWalker, heuristic, helper);
    }

    private Loader createLoader(MonitorFactory monitorFactory, XMLFactory xmlFactory) {

        LoaderRegistryImpl loader = new LoaderRegistryImpl(monitorFactory.getMonitor(LoaderRegistryImpl.Monitor.class), xmlFactory);

        LoaderHelper loaderHelper = new DefaultLoaderHelper();

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

        IncludeLoader includeLoader = new IncludeLoader(loader, null);
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

        JavaImplementationLoader javaLoader = new JavaImplementationLoader(processor, loaderHelper);
        mappedLoaders.put(JavaImplementation.IMPLEMENTATION_JAVA, javaLoader);
        return loader;
    }
}
