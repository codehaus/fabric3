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

import org.osoa.sca.CallableReference;
import org.osoa.sca.ServiceReference;

import org.fabric3.pojo.component.PojoComponent;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.services.proxy.ProxyService;

/**
 * The runtime instantiation of a Java component implementation.
 *
 * @version $Revision$ $Date$
 * @param <T> the implementation class for the defined component
 */
public class JavaComponent<T> extends PojoComponent<T> {
    private final ProxyService proxyService;

    /**
     * Constructor for a Java Component.
     *
     * @param componentId             the component's uri
     * @param instanceFactoryProvider the provider for the instance factory
     * @param scopeContainer          the container for the component's implementation scope
     * @param groupId                 the component group this component belongs to
     * @param initLevel               the initialization level
     * @param maxIdleTime             the time after which idle instances of this component can be expired
     * @param maxAge                  the time after which instances of this component can be expired
     * @param proxyService            the service used to create reference proxies
     */
    public JavaComponent(URI componentId,
                         InstanceFactoryProvider<T> instanceFactoryProvider,
                         ScopeContainer<?> scopeContainer,
                         URI groupId,
                         int initLevel,
                         long maxIdleTime,
                         long maxAge,
                         ProxyService proxyService) {
        super(componentId, instanceFactoryProvider, scopeContainer, groupId, initLevel, maxIdleTime, maxAge);
        this.proxyService = proxyService;
    }

    public <B> B getService(Class<B> businessInterface, String referenceName) {
        throw new UnsupportedOperationException();
    }

    public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, String referenceName) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public <B, R extends CallableReference<B>> R cast(B target) {
        return (R) proxyService.cast(target);
    }

}
