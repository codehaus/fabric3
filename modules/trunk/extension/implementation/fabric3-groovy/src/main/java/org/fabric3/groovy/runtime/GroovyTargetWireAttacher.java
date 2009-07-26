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
package org.fabric3.groovy.runtime;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.groovy.provision.GroovyTargetDefinition;
import org.fabric3.pojo.component.InvokerInterceptor;
import org.fabric3.pojo.component.PojoComponent;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * The component builder for Java implementation types. Responsible for creating the Component runtime artifact from a physical component definition
 *
 * @version $Rev$ $Date$
 */
public class GroovyTargetWireAttacher implements TargetWireAttacher<GroovyTargetDefinition> {
    private final ComponentManager manager;

    public GroovyTargetWireAttacher(@Reference ComponentManager manager) {
        this.manager = manager;
    }

    public void attach(PhysicalSourceDefinition sourceDefinition,
                               GroovyTargetDefinition targetDefinition,
                               Wire wire) throws WireAttachException {
        URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());
        Component component = manager.getComponent(targetName);
        assert component instanceof GroovyComponent;
        GroovyComponent<?> target = (GroovyComponent) component;

        ScopeContainer scopeContainer = target.getScopeContainer();
        Class<?> implementationClass = target.getImplementationClass();
        ClassLoader loader = implementationClass.getClassLoader();
        // attach the invoker interceptor to forward invocation chains
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            List<String> params = operation.getParameters();
            Class<?>[] paramTypes = new Class<?>[params.size()];
            assert loader != null;
            for (int i = 0; i < params.size(); i++) {
                String param = params.get(i);
                try {
                    paramTypes[i] = loader.loadClass(param);
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
            boolean endsConversation = operation.isEndsConversation();
            boolean callback = targetDefinition.isCallback();
            if (callback) {
                // callbacks do not expire the client (i.e. the callback target); they expire the forward implementation instance
                endsConversation = false;
            }
            InvokerInterceptor<?> interceptor = createInterceptor(method, callback, endsConversation, target, scopeContainer);
            chain.addInterceptor(interceptor);
        }

        // TODO handle callbacks
    }

    public void detach(PhysicalSourceDefinition source, GroovyTargetDefinition target) throws WiringException {
        // no-op
    }

    <T> InvokerInterceptor<T> createInterceptor(Method method,
                                                boolean callback,
                                                boolean endsConvesation,
                                                AtomicComponent<T> component,
                                                ScopeContainer scopeContainer) {
        return new InvokerInterceptor<T>(method, callback, endsConvesation, component, scopeContainer);
    }

    public ObjectFactory<?> createObjectFactory(GroovyTargetDefinition target) throws WiringException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        PojoComponent<?> targetComponent = (PojoComponent<?>) manager.getComponent(targetId);
        return targetComponent.createObjectFactory();
    }
}