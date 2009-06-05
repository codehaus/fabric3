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
package org.fabric3.web.runtime;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.model.type.java.InjectionSite;
import org.fabric3.pojo.builder.ProxyService;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.web.provision.WebComponentDefinition;

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
        URI componentId = definition.getComponentId();
        QName deployable = definition.getDeployable();
        // TODO fix properties
        Map<String, ObjectFactory<?>> propertyFactories = Collections.emptyMap();
        URI classLoaderId = definition.getClassLoaderId();
        Map<String, Map<String, InjectionSite>> injectorMappings = definition.getInjectionSiteMappings();
        ClassLoader cl = activator.getWebComponentClassLoader(classLoaderId);
        URI archiveUri = definition.getContributionUri();
        String contextUrl = definition.getContextUrl();
        return new WebComponent(componentId,
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
