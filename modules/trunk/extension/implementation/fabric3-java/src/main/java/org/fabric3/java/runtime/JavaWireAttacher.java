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
package org.fabric3.java.runtime;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.java.provision.JavaWireSourceDefinition;
import org.fabric3.java.provision.JavaWireTargetDefinition;
import org.fabric3.pojo.wire.PojoSourceWireAttacher;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches wires to and from components implemented using the Java programming model.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(interfaces = {SourceWireAttacher.class, TargetWireAttacher.class})
public class JavaWireAttacher extends PojoSourceWireAttacher implements SourceWireAttacher<JavaWireSourceDefinition>, TargetWireAttacher<JavaWireTargetDefinition> {

    private final SourceWireAttacherRegistry sourceWireAttacherRegistry;
    private final TargetWireAttacherRegistry targetWireAttacherRegistry;
    private final ComponentManager manager;
    private final ProxyService proxyService;
    private final ClassLoaderRegistry classLoaderRegistry;

    public JavaWireAttacher(@Reference ComponentManager manager,
                            @Reference SourceWireAttacherRegistry sourceWireAttacherRegistry,
                            @Reference TargetWireAttacherRegistry targetWireAttacherRegistry,
                            @Reference ProxyService proxyService,
                            @Reference ClassLoaderRegistry classLoaderRegistry,
                            @Reference(name = "transformerRegistry")TransformerRegistry<PullTransformer<?, ?>> transformerRegistry) {
        super(transformerRegistry, classLoaderRegistry);
        this.sourceWireAttacherRegistry = sourceWireAttacherRegistry;
        this.targetWireAttacherRegistry = targetWireAttacherRegistry;
        this.manager = manager;
        this.proxyService = proxyService;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Init
    public void init() {
        sourceWireAttacherRegistry.register(JavaWireSourceDefinition.class, this);
        targetWireAttacherRegistry.register(JavaWireTargetDefinition.class, this);
    }

    @Destroy
    public void destroy() {
        sourceWireAttacherRegistry.unregister(JavaWireSourceDefinition.class, this);
        targetWireAttacherRegistry.unregister(JavaWireTargetDefinition.class, this);
    }

    public void attachToSource(JavaWireSourceDefinition sourceDefinition, PhysicalWireTargetDefinition targetDefinition, Wire wire)
            throws WireAttachException {

        URI sourceUri = sourceDefinition.getUri();
        URI sourceName = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        JavaComponent<?> source = (JavaComponent) manager.getComponent(sourceName);
        InjectableAttribute injectableAttribute = sourceDefinition.getValueSource();

        Class<?> type;
        try {
            type = classLoaderRegistry.loadClass(sourceDefinition.getClassLoaderId(), sourceDefinition.getInterfaceName());
        } catch (ClassNotFoundException e) {
            String name = sourceDefinition.getInterfaceName();
            throw new WireAttachException("Unable to load interface class [" + name + "]", sourceUri, null, e);
        }
        if (InjectableAttributeType.CALLBACK.equals(injectableAttribute.getValueType())) {
            URI targetUri = targetDefinition.getUri();
            Scope scope = source.getScopeContainer().getScope();
            ObjectFactory<?> factory = proxyService.createCallbackObjectFactory(type, scope, targetUri, wire);
            // JFM TODO inject updates to object factory as this does not support a proxy fronting multiple callback wires
            source.setObjectFactory(injectableAttribute, factory);
        } else {
            String callbackUri = null;
            URI uri = targetDefinition.getCallbackUri();
            if (uri != null) {
                callbackUri = uri.toString();
            }
            boolean conversational = sourceDefinition.isConversational();
            ObjectFactory<?> factory = proxyService.createObjectFactory(type, conversational, wire, callbackUri);
            Object key = getKey(sourceDefinition, source, injectableAttribute);
            source.attachReferenceToTarget(injectableAttribute, factory, key);
        }
    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition, JavaWireTargetDefinition targetDefinition, Wire wire)
            throws WireAttachException {
        URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());
        Component component = manager.getComponent(targetName);
        assert component instanceof JavaComponent;

        JavaComponent<?> target = (JavaComponent<?>) component;

        ScopeContainer<?> scopeContainer = target.getScopeContainer();
        Class<?> implementationClass = target.getImplementationClass();
        ClassLoader loader = implementationClass.getClassLoader();

        // attach the invoker interceptor to forward invocation chains
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            PhysicalOperationDefinition operation = entry.getKey();
            InvocationChain chain = entry.getValue();
            List<String> params = operation.getParameters();
            Class<?>[] paramTypes = new Class<?>[params.size()];
            assert loader != null;
            for (int i = 0; i < params.size(); i++) {
                String param = params.get(i);
                try {
                    paramTypes[i] = classLoaderRegistry.loadClass(loader, param);
                } catch (ClassNotFoundException e) {
                    URI sourceUri = sourceDefinition.getUri();
                    URI targetUri = targetDefinition.getUri();
                    throw new WireAttachException("Implementation class not found", sourceUri, targetUri, e);
                }
            }
            Method method;
            try {
                method = implementationClass.getMethod(operation.getName(), paramTypes);
            } catch (NoSuchMethodException e) {
                URI sourceUri = sourceDefinition.getUri();
                URI targetUri = targetDefinition.getUri();
                throw new WireAttachException("No matching method found", sourceUri, targetUri, e);
            }
            chain.addInterceptor(createInterceptor(method, targetDefinition.isCallback(), operation.isEndsConversation(), target, scopeContainer));
        }
    }

    public void attachObjectFactory(JavaWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        URI sourceId = UriHelper.getDefragmentedName(source.getUri());
        JavaComponent<?> sourceComponent = (JavaComponent<?>) manager.getComponent(sourceId);
        InjectableAttribute injectableAttribute = source.getValueSource();

        Object key = getKey(source, sourceComponent, injectableAttribute);
        sourceComponent.attachReferenceToTarget(injectableAttribute, objectFactory, key);
    }

    public ObjectFactory<?> createObjectFactory(JavaWireTargetDefinition target) throws WiringException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        JavaComponent<?> targetComponent = (JavaComponent<?>) manager.getComponent(targetId);
        return targetComponent.createObjectFactory();
    }
}
