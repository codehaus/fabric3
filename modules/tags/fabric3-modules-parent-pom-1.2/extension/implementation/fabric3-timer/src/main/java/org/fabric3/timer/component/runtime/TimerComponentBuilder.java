/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.timer.component.runtime;

import java.net.URI;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.Scope;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.pojo.builder.PojoComponentBuilder;
import org.fabric3.pojo.builder.ProxyService;
import org.fabric3.pojo.component.OASISPojoComponentContext;
import org.fabric3.pojo.component.OASISPojoRequestContext;
import org.fabric3.pojo.component.PojoComponentContext;
import org.fabric3.pojo.component.PojoRequestContext;
import org.fabric3.pojo.injection.ConversationIDObjectFactory;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.instancefactory.InstanceFactoryProvider;
import org.fabric3.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.timer.component.provision.TimerComponentDefinition;
import org.fabric3.timer.component.provision.TriggerData;
import org.fabric3.timer.spi.TimerService;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class TimerComponentBuilder<T> extends PojoComponentBuilder<T, TimerComponentDefinition, TimerComponent<?>> {
    private TimerService nonTrxTimerService;
    private TimerService trxTimerService;
    private ProxyService proxyService;

    public TimerComponentBuilder(@Reference ScopeRegistry scopeRegistry,
                                 @Reference InstanceFactoryBuilderRegistry providerBuilders,
                                 @Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference(name = "transformerRegistry") TransformerRegistry<PullTransformer<?, ?>> transformerRegistry,
                                 @Reference ProxyService proxyService,
                                 @Reference(name = "nonTrxTimerService") TimerService nonTrxTimerService,
                                 @Reference(name = "trxTimerService") TimerService trxTimerService) {
        super(scopeRegistry, providerBuilders, classLoaderRegistry, transformerRegistry);
        this.proxyService = proxyService;
        this.nonTrxTimerService = nonTrxTimerService;
        this.trxTimerService = trxTimerService;
    }

    public TimerComponent<T> build(TimerComponentDefinition definition) throws BuilderException {
        URI uri = definition.getComponentUri();
        int initLevel = definition.getInitLevel();
        QName deployable = definition.getDeployable();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());

        // get the scope container for this component
        String scopeName = definition.getScope();
        Scope<?> scope = scopeRegistry.getScope(scopeName);
        ScopeContainer scopeContainer = scopeRegistry.getScopeContainer(scope);

        // create the InstanceFactoryProvider based on the definition in the model
        InstanceFactoryDefinition providerDefinition = definition.getProviderDefinition();


        InstanceFactoryProvider<T> provider = providerBuilders.build(providerDefinition, classLoader);

        createPropertyFactories(definition, provider);
        TriggerData data = definition.getTriggerData();
        TimerService timerService;
        if (definition.isTransactional()) {
            timerService = trxTimerService;
        } else {
            timerService = nonTrxTimerService;
        }
        TimerComponent<T> component = new TimerComponent<T>(uri,
                                                            provider,
                                                            scopeContainer,
                                                            deployable,
                                                            initLevel,
                                                            definition.getMaxIdleTime(),
                                                            definition.getMaxAge(),
                                                            proxyService,
                                                            data,
                                                            timerService);

        PojoRequestContext requestContext = new PojoRequestContext();
        provider.setObjectFactory(InjectableAttribute.REQUEST_CONTEXT, new SingletonObjectFactory<PojoRequestContext>(requestContext));
        PojoComponentContext componentContext = new PojoComponentContext(component, requestContext);
        provider.setObjectFactory(InjectableAttribute.COMPONENT_CONTEXT, new SingletonObjectFactory<PojoComponentContext>(componentContext));
        provider.setObjectFactory(InjectableAttribute.CONVERSATION_ID, new ConversationIDObjectFactory());

        OASISPojoRequestContext oasisRequestContext = new OASISPojoRequestContext();
        provider.setObjectFactory(InjectableAttribute.OASIS_REQUEST_CONTEXT,
                                  new SingletonObjectFactory<OASISPojoRequestContext>(oasisRequestContext));
        OASISPojoComponentContext oasisComponentContext = new OASISPojoComponentContext(component, oasisRequestContext);
        provider.setObjectFactory(InjectableAttribute.OASIS_COMPONENT_CONTEXT,
                                  new SingletonObjectFactory<OASISPojoComponentContext>(oasisComponentContext));

        return component;

    }

}
