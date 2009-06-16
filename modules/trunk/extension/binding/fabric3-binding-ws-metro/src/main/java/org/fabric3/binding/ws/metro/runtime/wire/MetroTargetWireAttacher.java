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
package org.fabric3.binding.ws.metro.runtime.wire;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.metro.provision.MetroWireTargetDefinition;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.runtime.core.MetroTargetInterceptor;
import org.fabric3.binding.ws.metro.runtime.policy.FeatureResolver;
import org.fabric3.host.work.WorkScheduler;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Creates invocation chains for invoking a target web service.
 */
public class MetroTargetWireAttacher implements TargetWireAttacher<MetroWireTargetDefinition> {
    private ClassLoaderRegistry registry;
    private FeatureResolver resolver;
    private WorkScheduler scheduler;

    public MetroTargetWireAttacher(@Reference ClassLoaderRegistry registry, @Reference FeatureResolver resolver, @Reference WorkScheduler scheduler) {
        this.registry = registry;
        this.resolver = resolver;
        this.scheduler = scheduler;
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, MetroWireTargetDefinition target, Wire wire) throws WiringException {

        try {
            ReferenceEndpointDefinition endpointDefinition = target.getEndpointDefinition();
            QName serviceName = endpointDefinition.getServiceName();
            URL url = endpointDefinition.getUrl();
            String interfaze = target.getInterface();
            URI classLoaderId = source.getClassLoaderId();
            List<QName> requestedIntents = target.getRequestedIntents();
            List<PolicySet> requestedPolicySets = null;

            ClassLoader classLoader = registry.getClassLoader(classLoaderId);
            WebServiceFeature[] features = resolver.getFeatures(requestedIntents, requestedPolicySets);

            Class<?> seiClass = classLoader.loadClass(interfaze);

            ObjectFactory<?> proxyFactory = new LazyProxyObjectFactory(url, serviceName, seiClass, features, scheduler);

            Method[] methods = seiClass.getDeclaredMethods();
            for (InvocationChain chain : wire.getInvocationChains()) {
                Method method = null;
                for (Method meth : methods) {
                    if (chain.getPhysicalOperation().getName().equals(meth.getName())) {
                        method = meth;
                        break;
                    }
                }
                MetroTargetInterceptor targetInterceptor = new MetroTargetInterceptor(proxyFactory, method);
                chain.addInterceptor(targetInterceptor);
            }
        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        }

    }

    public ObjectFactory<?> createObjectFactory(MetroWireTargetDefinition target) throws WiringException {
        return null;
    }

    public void detachFromTarget(PhysicalWireSourceDefinition source, MetroWireTargetDefinition target) throws WiringException {
    }

}
