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

import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.ws.api.BindingID;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.metro.provision.MetroWireSourceDefinition;
import org.fabric3.binding.ws.metro.runtime.core.F3Invoker;
import org.fabric3.binding.ws.metro.runtime.core.MetroServlet;
import org.fabric3.binding.ws.metro.runtime.policy.BindingIdResolver;
import org.fabric3.binding.ws.metro.runtime.policy.FeatureResolver;
import org.fabric3.binding.ws.provision.WsdlElement;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Source wire attacher that provisions services.
 */
public class MetroSourceWireAttacher implements SourceWireAttacher<MetroWireSourceDefinition> {

    @Reference
    protected ServletHost servletHost;
    @Reference
    protected ClassLoaderRegistry classLoaderRegistry;
    @Reference
    protected FeatureResolver featureResolver;
    @Reference
    protected BindingIdResolver bindingIdResolver;

    private MetroServlet metroServlet = new MetroServlet();

    /**
     * Registers the servlet.
     */
    @Init
    public void start() {
        servletHost.registerMapping("/metro/*", metroServlet);

    }

    /**
     * Not supported.
     */
    public void attachObjectFactory(MetroWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target) {
        throw new UnsupportedOperationException();
    }

    /**
     * Provisions the service.
     */
    public void attachToSource(MetroWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {

        try {

            URI servicePath = source.getServicePath();
            WsdlElement wsdlElement = source.getWsdlElement();
            List<InvocationChain> invocationChains = wire.getInvocationChains();
            URI classLoaderId = source.getClassLoaderId();
            String interfaze = source.getInterfaze();
            URL wsdlUrl = source.getWsdlUrl();
            List<QName> requestedIntents = source.getRequestedIntents();
            List<PolicySet> requestedPolicySets = null;

            ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);
            // load the application class
            Class<?> sei = classLoader.loadClass(interfaze);

            // Metro requires a Metro proxy interface to be visible to the classloader that loaded the SEI class
            // To enable this, dynamically add the Metro extension classloader as a parent to the classloader that loaded the SEI class if the host
            // supports classloader isolation. Note that the latter may be different than the application classloader (e.g. in the Maven iTest
            // runtime, the SEI class will be loaded by the host classloader, not the classloader representing the application
            ClassLoader seiClassLoader = sei.getClassLoader();
            ClassLoader extensionClassLoader = getClass().getClassLoader();
            if (seiClassLoader instanceof MultiParentClassLoader) {
                MultiParentClassLoader multiParentClassLoader = (MultiParentClassLoader) seiClassLoader;
                if (!multiParentClassLoader.getParents().contains(extensionClassLoader)) {
                    multiParentClassLoader.addParent(extensionClassLoader);
                }
            }
            WebServiceFeature[] features = featureResolver.getFeatures(requestedIntents, requestedPolicySets);
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            BindingID bindingID;
            try {
                Thread.currentThread().setContextClassLoader(extensionClassLoader);
                bindingID = bindingIdResolver.resolveBindingId(requestedIntents, requestedPolicySets);
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
            F3Invoker f3Invoker = new F3Invoker(invocationChains);

            metroServlet.registerService(sei, wsdlUrl, "/metro" + servicePath.toASCIIString(), wsdlElement, f3Invoker, features, bindingID);

        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        }

    }

    /**
     * Not supported.
     */
    public void detachFromSource(MetroWireSourceDefinition source, PhysicalWireTargetDefinition target) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unprovisions the service.
     */
    public void detachObjectFactory(MetroWireSourceDefinition source, PhysicalWireTargetDefinition target) {
    }

}
