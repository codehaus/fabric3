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
package org.fabric3.implementation.web.runtime;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.implementation.web.provision.WebComponentDefinition;
import org.fabric3.implementation.pojo.builder.ProxyService;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.model.type.java.InjectionSite;

/**
 * Instantiates a web component on a runtime node.
 */
@EagerInit
public class WebComponentBuilder implements ComponentBuilder<WebComponentDefinition, WebComponent> {
    private WebApplicationActivator activator;
    private InjectorFactory injectorFactory;
    private ProxyService proxyService;

    public WebComponentBuilder(@Reference ProxyService proxyService,
                               @Reference WebApplicationActivator activator,
                               @Reference InjectorFactory injectorFactory) {
        this.proxyService = proxyService;
        this.activator = activator;
        this.injectorFactory = injectorFactory;
    }

    public WebComponent build(WebComponentDefinition definition) throws BuilderException {
        URI uri = definition.getComponentUri();
        QName deployable = definition.getDeployable();
        // TODO fix properties
        Map<String, ObjectFactory<?>> propertyFactories = Collections.emptyMap();
        URI classLoaderId = definition.getClassLoaderId();
        Map<String, Map<String, InjectionSite>> injectorMappings = definition.getInjectionSiteMappings();
        ClassLoader cl = activator.getWebComponentClassLoader(classLoaderId);
        URI archiveUri = definition.getContributionUri();
        String contextUrl = definition.getContextUrl();
        return new WebComponent(uri,
                                contextUrl,
                                deployable,
                                archiveUri,
                                cl,
                                injectorFactory,
                                activator,
                                proxyService,
                                propertyFactories,
                                injectorMappings);
    }

}
