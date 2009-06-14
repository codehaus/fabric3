/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
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
import org.fabric3.binding.ws.metro.runtime.core.TargetInterceptor;
import org.fabric3.binding.ws.metro.runtime.policy.FeatureResolver;
import org.fabric3.binding.ws.provision.WsdlElement;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Provides the infrastructure for invoking a target web service.
 */
public class MetroTargetWireAttacher implements TargetWireAttacher<MetroWireTargetDefinition> {

    @Reference
    protected ClassLoaderRegistry classLoaderRegistry;
    @Reference
    protected FeatureResolver featureResolver;

    /**
     * Attaches to the target.
     */
    public void attachToTarget(PhysicalWireSourceDefinition source, MetroWireTargetDefinition target, Wire wire) throws WiringException {

        try {

            WsdlElement wsdlElement = target.getWsdlElement();
            URL[] referenceUrls = target.getTargetUrls();
            String interfaze = target.getInterfaze();
            URI classLoaderId = source.getClassLoaderId();
            List<QName> requestedIntents = target.getRequestedIntents();
            List<PolicySet> requestedPolicySets = null;

            ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);
            WebServiceFeature[] features = featureResolver.getFeatures(requestedIntents, requestedPolicySets);

            Class<?> sei = classLoader.loadClass(interfaze);

            // Metro requires library classes to be visibile to the application classloader. If executing in an environment that supports classloader
            // isolation, dynamically update the application classloader by setting a parent to the Metro classloader.
            ClassLoader seiClassLoader = sei.getClassLoader();
            if (seiClassLoader instanceof MultiParentClassLoader) {
                MultiParentClassLoader multiParentClassLoader = (MultiParentClassLoader) seiClassLoader;
                ClassLoader extensionCl = getClass().getClassLoader();
                if (!multiParentClassLoader.getParents().contains(extensionCl)) {
                    multiParentClassLoader.addParent(extensionCl);
                }
            }
            Method[] methods = sei.getDeclaredMethods();

            for (InvocationChain chain : wire.getInvocationChains()) {
                Method method = null;
                for (Method meth : methods) {
                    if (chain.getPhysicalOperation().getName().equals(meth.getName())) {
                        method = meth;
                        break;
                    }
                }
                TargetInterceptor targetInterceptor = new TargetInterceptor(wsdlElement, sei, referenceUrls, seiClassLoader, method, features);
                chain.addInterceptor(targetInterceptor);
            }

        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        }

    }

    /**
     * Creates an object factory.
     */
    public ObjectFactory<?> createObjectFactory(MetroWireTargetDefinition target) throws WiringException {
        return null;
    }

    /**
     * Detach from the target.
     */
    public void detachFromTarget(PhysicalWireSourceDefinition source, MetroWireTargetDefinition target) throws WiringException {
    }

}
