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
package org.fabric3.groovy.runtime;

import java.net.URI;

import org.fabric3.pojo.component.PojoComponent;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.component.ScopeContainer;

/**
 * Runtime container for a component implemented in Groovy.
 *
 * @version $Rev$ $Date$
 */
public class GroovyComponent<T> extends PojoComponent<T> {
    public GroovyComponent(URI componentId,
                           InstanceFactoryProvider<T> instanceFactoryProvider,
                           ScopeContainer<?> scopeContainer,
                           URI groupId,
                           int initLevel,
                           long maxIdleTime,
                           long maxAge) {
        super(componentId, instanceFactoryProvider, scopeContainer, groupId, initLevel, maxIdleTime, maxAge);
    }
}
