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
package org.fabric3.spring;

import org.fabric3.pojo.builder.PojoComponentBuilder;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.transform.PullTransformer;
import org.fabric3.transform.TransformerRegistry;
import org.fabric3.spi.services.proxy.ProxyService;
import org.fabric3.spring.applicationContext.SCAApplicationContext;
import org.fabric3.spring.applicationContext.SCAParentApplicationContext;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * The component builder for Spring implementation types. Responsible for creating the Component runtime artifact from a
 * physical component definition
 *
 * @version $Rev$ $Date$
 * @param <T> the implementation class for the defined component
 */
@EagerInit
public class SpringComponentBuilder<T> extends PojoComponentBuilder<T, SpringComponentDefinition, SpringComponent<T>> {

    public SpringComponentBuilder(@Reference ComponentBuilderRegistry builderRegistry,
                                @Reference ScopeRegistry scopeRegistry,
                                @Reference InstanceFactoryBuilderRegistry providerBuilders,
                                @Reference ClassLoaderRegistry classLoaderRegistry,
                                @Reference(name = "transformerRegistry") TransformerRegistry<PullTransformer<?, ?>> transformerRegistry,
                                @Reference ProxyService proxyService) {
        super(builderRegistry, scopeRegistry, providerBuilders, classLoaderRegistry, transformerRegistry);
    }

    @Init
    public void init() {
        builderRegistry.register(SpringComponentDefinition.class, this);
    }

    public SpringComponent<T> build(SpringComponentDefinition componentDefinition) throws BuilderException {
        String springBeanId = componentDefinition.getSpringBeanId();

        ClassLoader classLoader = classLoaderRegistry.getClassLoader(componentDefinition.getClassLoaderId());


        SCAParentApplicationContext parentAC =
            new SCAParentApplicationContext(classLoader, componentDefinition);
        SCAApplicationContext scaApplicationContext =
            new SCAApplicationContext(parentAC, componentDefinition.getResource(), classLoader);
        
        ObjectFactory<T> objectFactory = new SpringObjectFactory<T>(scaApplicationContext, springBeanId);

        SpringComponent<T> springComponent = new SpringComponent<T>(componentDefinition.getComponentId(), objectFactory);
        
        parentAC.setSpringComponent(springComponent);
        
        return springComponent;

    }
}
