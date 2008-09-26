/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.java.runtime;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.java.provision.JavaComponentDefinition;
import org.fabric3.pojo.builder.PojoComponentBuilder;
import org.fabric3.pojo.component.PojoComponentContext;
import org.fabric3.pojo.component.PojoRequestContext;
import org.fabric3.pojo.injection.ConversationIDObjectFactory;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.proxy.ProxyService;
import org.fabric3.transform.PullTransformer;
import org.fabric3.transform.TransformerRegistry;

/**
 * Builds a JavaComponent from a physical definition.
 *
 * @version $Rev$ $Date$
 * @param <T> the implementation class for the defined component
 */
@EagerInit
public class JavaComponentBuilder<T> extends PojoComponentBuilder<T, JavaComponentDefinition, JavaComponent<T>> {
    private ProxyService proxyService;

    public JavaComponentBuilder(@Reference ComponentBuilderRegistry builderRegistry,
                                @Reference ScopeRegistry scopeRegistry,
                                @Reference InstanceFactoryBuilderRegistry providerBuilders,
                                @Reference ClassLoaderRegistry classLoaderRegistry,
                                @Reference(name = "transformerRegistry")TransformerRegistry<PullTransformer<?, ?>> transformerRegistry,
                                @Reference ProxyService proxyService) {
        super(builderRegistry, scopeRegistry, providerBuilders, classLoaderRegistry, transformerRegistry);
        this.proxyService = proxyService;
    }

    @Init
    public void init() {
        builderRegistry.register(JavaComponentDefinition.class, this);
    }

    public JavaComponent<T> build(JavaComponentDefinition definition) throws BuilderException {
        URI componentId = definition.getComponentId();
        int initLevel = definition.getInitLevel();
        URI groupId = definition.getGroupId();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());

        // get the scope container for this component
        String scopeName = definition.getScope();
        Scope<?> scope = scopeRegistry.getScope(scopeName);
        ScopeContainer<?> scopeContainer = scopeRegistry.getScopeContainer(scope);

        // create the InstanceFactoryProvider based on the definition in the model
        InstanceFactoryDefinition providerDefinition = definition.getProviderDefinition();


        InstanceFactoryProvider<T> provider = providerBuilders.build(providerDefinition, classLoader);

        createPropertyFactories(definition, provider);

        JavaComponent<T> component = new JavaComponent<T>(componentId,
                                                          provider,
                                                          scopeContainer,
                                                          groupId,
                                                          initLevel,
                                                          definition.getMaxIdleTime(),
                                                          definition.getMaxAge(),
                                                          proxyService);

        PojoRequestContext requestContext = new PojoRequestContext();
        provider.setObjectFactory(InjectableAttribute.REQUEST_CONTEXT, new SingletonObjectFactory<PojoRequestContext>(requestContext));

        PojoComponentContext componentContext = new PojoComponentContext(component, requestContext);
        provider.setObjectFactory(InjectableAttribute.COMPONENT_CONTEXT, new SingletonObjectFactory<PojoComponentContext>(componentContext));
        provider.setObjectFactory(InjectableAttribute.CONVERSATION_ID, new ConversationIDObjectFactory());

        return component;

    }
}
