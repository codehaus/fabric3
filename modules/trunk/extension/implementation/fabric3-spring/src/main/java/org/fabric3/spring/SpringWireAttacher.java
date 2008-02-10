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
package org.fabric3.spring;

import java.net.URI;
import java.util.Map;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.scdl.Signature;
import org.fabric3.pojo.wire.PojoWireAttacher;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.scdl.ValueSource;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.wire.Wire;

/**
 * The component builder for Spring implementation types. Responsible for creating the Component runtime artifact from a
 * physical component definition
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(interfaces={SourceWireAttacher.class, TargetWireAttacher.class})
public class SpringWireAttacher extends PojoWireAttacher implements SourceWireAttacher<SpringWireSourceDefinition>, TargetWireAttacher<SpringWireTargetDefinition> {
    private final SourceWireAttacherRegistry sourceWireAttacherRegistry;
    private final TargetWireAttacherRegistry targetWireAttacherRegistry;
    private final ComponentManager manager;
    private final ProxyService proxyService;
    private final ClassLoaderRegistry classLoaderRegistry;

    private boolean debug = false;

    public SpringWireAttacher(@Reference ComponentManager manager,
                              @Reference SourceWireAttacherRegistry sourceWireAttacherRegistry,
                              @Reference TargetWireAttacherRegistry targetWireAttacherRegistry,
                              @Reference ProxyService proxyService,
                              @Reference ClassLoaderRegistry classLoaderRegistry,
                              @Reference(name = "transformerRegistry")
                              TransformerRegistry<PullTransformer<?, ?>> transformerRegistry) {
        super(transformerRegistry, classLoaderRegistry);
        this.sourceWireAttacherRegistry = sourceWireAttacherRegistry;
        this.targetWireAttacherRegistry = targetWireAttacherRegistry;
        this.manager = manager;
        this.proxyService = proxyService;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Init
    public void init() {
        sourceWireAttacherRegistry.register(SpringWireSourceDefinition.class, this);
        targetWireAttacherRegistry.register(SpringWireTargetDefinition.class, this);
    }

    @Destroy
    public void destroy() {
        sourceWireAttacherRegistry.unregister(SpringWireSourceDefinition.class, this);
        targetWireAttacherRegistry.unregister(SpringWireTargetDefinition.class, this);
    }

    public void attachToSource(SpringWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) {

        URI sourceUri = sourceDefinition.getUri();
        URI sourceName = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        Component component = manager.getComponent(sourceName);
        assert component instanceof SpringComponent;
        SpringComponent<?> source = (SpringComponent) component;
        ValueSource referenceSource = new ValueSource(ValueSource.ValueSourceType.REFERENCE, sourceUri.getFragment());

        Class<?> type = sourceDefinition.getFieldType();
        URI targetUri = targetDefinition.getUri();

        Component target = null;
        if (targetUri != null) {
            URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());
            target = manager.getComponent(targetName);
        }

        if (debug)
            System.out.println("##############SpringWireAttacher:attachToSource()" +
                    "; sourceUri=" + sourceUri + "; sourceName=" + sourceName +
                    "; targetUri=" + targetUri + "; targetName=" + UriHelper.getDefragmentedName(targetDefinition.getUri()) +
                    "; sourceUri.getFragment()=" + sourceUri.getFragment());

//        Object key = getKey(sourceDefinition, source, referenceSource);

            assert target instanceof AtomicComponent;
//            ObjectFactory<?> factory = ((AtomicComponent<?>) target).createObjectFactory();

//            source.setObjectFactory(referenceSource, factory);
//            if (target != null) {
//                source.attachReferenceToTarget(referenceSource, factory, key);
//            }
//        } else {
            ObjectFactory<?> factory = null;
//            ClassLoader cl = getClass().getClassLoader();
            ClassLoader cl = classLoaderRegistry.getClassLoader(sourceDefinition.getClassLoaderId());

            try {
                factory = createWireObjectFactory(cl.loadClass(type.getName()),
                                                  sourceDefinition.isConversational(),
                                                  wire);
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String refName = sourceUri.getFragment();
//            if (target != null) {
//                source.attachReferenceToTarget(referenceSource, factory, key);
//            } else {
            source.addRefNameToObjFactory(refName, factory);
//            }
//            if (!wire.getCallbackInvocationChains().isEmpty()) {
//                URI callbackUri = sourceDefinition.getCallbackUri();
//                ValueSource callbackSource =
//                        new ValueSource(ValueSource.ValueSourceType.SERVICE, callbackUri.getFragment());
//                Class<?> callbackType = source.getMemberType(callbackSource);
//                source.setObjectFactory(callbackSource, createCallbackWireObjectFactory(callbackType));
//            }

    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               SpringWireTargetDefinition targetDefinition,
                               Wire wire) throws WireAttachException {
//        if (sourceDefinition.isOptimizable()) {
//            return;
//        }
        URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());
        Component component = manager.getComponent(targetName);
        assert component instanceof SpringComponent;
        SpringComponent<?> target = (SpringComponent) component;

        if (debug)
            System.out.println("##############in SpringWireAttacher:attachToTarget" +
                    "; t.uri=" + targetDefinition.getUri() + "; targetName=" + targetName +
                    "; s.uri=" + sourceDefinition.getUri() + "; s.key=" + sourceDefinition.getKey() +
                    "; size=" + wire.getInvocationChains().entrySet().size());

        // not used yet
        String beanId = targetDefinition.getBeanId();

        // attach the invoker interceptor to forward invocation chains
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            PhysicalOperationDefinition operation = entry.getKey();

            Signature signature = new Signature(operation.getName(), operation.getParameters());

            if (debug)
                System.out.println("##############in SpringWireAttacher operation=" + operation.getName());

            Interceptor targetInterceptor = new SpringTargetInterceptor(signature, target);
            InvocationChain chain = entry.getValue();
            chain.addInterceptor(targetInterceptor);
        }
    }

    private <T> ObjectFactory<T> createWireObjectFactory(Class<T> type, boolean isConversational, Wire wire) {
        return proxyService.createObjectFactory(type, isConversational, wire);
    }

    private <T> ObjectFactory<T> createCallbackWireObjectFactory(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    public void attachObjectFactory(SpringWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }

    public ObjectFactory<?> createObjectFactory(SpringWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }
}
