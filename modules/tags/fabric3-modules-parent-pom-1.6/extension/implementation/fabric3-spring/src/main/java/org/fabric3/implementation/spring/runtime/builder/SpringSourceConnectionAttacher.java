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
package org.fabric3.implementation.spring.runtime.builder;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.implementation.pojo.builder.ChannelProxyService;
import org.fabric3.implementation.pojo.builder.ProxyCreationException;
import org.fabric3.implementation.spring.provision.SpringConnectionSourceDefinition;
import org.fabric3.implementation.spring.runtime.component.SpringComponent;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.component.ConnectionAttachException;
import org.fabric3.spi.builder.component.SourceConnectionAttacher;
import org.fabric3.spi.channel.ChannelConnection;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.util.UriHelper;

/**
 * Attaches and detaches a {@link ChannelConnection} from a Spring component producer.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class SpringSourceConnectionAttacher implements SourceConnectionAttacher<SpringConnectionSourceDefinition> {
    private ComponentManager manager;
    private ChannelProxyService proxyService;
    private ClassLoaderRegistry classLoaderRegistry;

    public SpringSourceConnectionAttacher(@Reference ComponentManager manager,
                                          @Reference ChannelProxyService proxyService,
                                          @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.manager = manager;
        this.proxyService = proxyService;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attach(SpringConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target, ChannelConnection connection)
            throws ConnectionAttachException {
        URI sourceUri = source.getSourceUri();
        URI sourceName = UriHelper.getDefragmentedName(sourceUri);
        SpringComponent component = (SpringComponent) manager.getComponent(sourceName);
        if (component == null) {
            throw new ConnectionAttachException("Source component not found: " + sourceName);
        }
        Class<?> type;
        try {
            type = classLoaderRegistry.loadClass(source.getClassLoaderId(), source.getInterfaceName());
        } catch (ClassNotFoundException e) {
            String name = source.getInterfaceName();
            throw new ConnectionAttachException("Unable to load interface class: " + name, e);
        }
        try {
            ObjectFactory<?> factory = proxyService.createObjectFactory(type, connection);
            component.attach(source.getProducerName(), type, factory);
        } catch (ProxyCreationException e) {
            throw new ConnectionAttachException(e);
        }
    }

    public void detach(SpringConnectionSourceDefinition source, PhysicalConnectionTargetDefinition target) throws ConnectionAttachException {
        URI sourceName = UriHelper.getDefragmentedName(source.getSourceUri());
        SpringComponent component = (SpringComponent) manager.getComponent(sourceName);
        component.detach(source.getProducerName());
    }


}