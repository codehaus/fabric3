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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.ws.api.BindingID;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.metro.provision.MetroWireSourceDefinition;
import org.fabric3.binding.ws.metro.provision.PolicyExpressionMapping;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.binding.ws.metro.runtime.core.MetroServiceInvoker;
import org.fabric3.binding.ws.metro.runtime.core.MetroServlet;
import org.fabric3.binding.ws.metro.runtime.policy.BindingIdResolver;
import org.fabric3.binding.ws.metro.runtime.policy.FeatureResolver;
import org.fabric3.binding.ws.metro.runtime.policy.GeneratedArtifacts;
import org.fabric3.binding.ws.metro.runtime.policy.PolicyAttachmentException;
import org.fabric3.binding.ws.metro.runtime.policy.WsdlGenerationException;
import org.fabric3.binding.ws.metro.runtime.policy.WsdlGenerator;
import org.fabric3.binding.ws.metro.runtime.policy.WsdlPolicyAttacher;
import org.fabric3.host.work.WorkScheduler;
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
 *
 * @version $Rev$ $Date$
 */
public class MetroSourceWireAttacher implements SourceWireAttacher<MetroWireSourceDefinition> {
    private ServletHost servletHost;
    private ClassLoaderRegistry classLoaderRegistry;
    private FeatureResolver featureResolver;
    private BindingIdResolver bindingIdResolver;
    private InterfaceGenerator interfaceGenerator;
    private WsdlGenerator wsdlGenerator;
    private WsdlPolicyAttacher policyAttacher;
    private WorkScheduler scheduler;

    private MetroServlet metroServlet;

    public MetroSourceWireAttacher(@Reference ServletHost servletHost,
                                   @Reference ClassLoaderRegistry classLoaderRegistry,
                                   @Reference FeatureResolver featureResolver,
                                   @Reference BindingIdResolver bindingIdResolver,
                                   @Reference InterfaceGenerator interfaceGenerator,
                                   @Reference WsdlGenerator wsdlGenerator,
                                   @Reference WsdlPolicyAttacher policyAttacher,
                                   @Reference WorkScheduler scheduler) {
        this.servletHost = servletHost;
        this.classLoaderRegistry = classLoaderRegistry;
        this.featureResolver = featureResolver;
        this.bindingIdResolver = bindingIdResolver;
        this.interfaceGenerator = interfaceGenerator;
        this.wsdlGenerator = wsdlGenerator;
        this.policyAttacher = policyAttacher;
        this.scheduler = scheduler;
    }

    @Init
    public void init() {
        metroServlet = new MetroServlet(scheduler);
    }

    public void attachToSource(MetroWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        try {
            ServiceEndpointDefinition endpointDefinition = source.getEndpointDefinition();
            QName serviceName = endpointDefinition.getServiceName();
            QName portName = endpointDefinition.getPortName();
            URI servicePath = endpointDefinition.getServicePath();
            List<InvocationChain> invocationChains = wire.getInvocationChains();
            URI classLoaderId = source.getClassLoaderId();
            String interfaze = source.getInterface();
            URL wsdlUrl = source.getWsdlLocation();
            List<QName> requestedIntents = source.getRequestedIntents();
            List<PolicySet> requestedPolicySets = null;

            ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);

            // load the service interface
            Class<?> seiClass = classLoader.loadClass(interfaze);
            if (!seiClass.isAnnotationPresent(WebService.class)) {
                // if the service interface is not annotated, generate an implementing class that is
                // TODO make sure the WSDL is correct
                seiClass = interfaceGenerator.generateAnnotatedInterface(seiClass, null, null, null, null);
            }
            File generatedWsdl = null;
            List<File> generatedSchemas = null;
            if (!source.getMappings().isEmpty()) {
                // if policy is configured for the endpoint, generate a WSDL with the policy attachments
                GeneratedArtifacts artifacts = wsdlGenerator.generate(seiClass, serviceName);
                generatedWsdl = artifacts.getWsdl();
                generatedSchemas = artifacts.getSchemas();
                for (PolicyExpressionMapping mapping : source.getMappings()) {
                    policyAttacher.attach(generatedWsdl, mapping.getOperations(), mapping.getPolicyExpression());
                }
                wsdlUrl = generatedWsdl.toURI().toURL();
            }
            ClassLoader extensionClassLoader = updateClassLoader(seiClass);

            WebServiceFeature[] features = featureResolver.getFeatures(requestedIntents, requestedPolicySets);
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            BindingID bindingID;
            try {
                Thread.currentThread().setContextClassLoader(extensionClassLoader);
                bindingID = bindingIdResolver.resolveBindingId(requestedIntents, requestedPolicySets);
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }

            MetroServiceInvoker invoker = new MetroServiceInvoker(invocationChains);

            // FIXME remove need to decode
            String path = URLDecoder.decode(servicePath.toASCIIString(), "UTF-8");
            servletHost.registerMapping(path, metroServlet);
            // register <endpoint-url/mex> address for serving WS-MEX requests
            servletHost.registerMapping(path + "/mex", metroServlet);

            metroServlet.registerService(seiClass,
                                         serviceName,
                                         portName,
                                         wsdlUrl,
                                         path,
                                         invoker,
                                         features,
                                         bindingID,
                                         generatedWsdl,
                                         generatedSchemas);

        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        } catch (UnsupportedEncodingException e) {
            throw new WiringException(e);
        } catch (WsdlGenerationException e) {
            throw new WiringException(e);
        } catch (PolicyAttachmentException e) {
            throw new WiringException(e);
        } catch (MalformedURLException e) {
            throw new WiringException(e);
        }
    }

    public void detachFromSource(MetroWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        try {
            ServiceEndpointDefinition endpointDefinition = source.getEndpointDefinition();
            URI servicePath = endpointDefinition.getServicePath();
            // FIXME remove need to decode
            String path = URLDecoder.decode(servicePath.toASCIIString(), "UTF-8");
            metroServlet.unregisterService(path);
        } catch (UnsupportedEncodingException e) {
            throw new WiringException(e);
        }
    }

    public void detachObjectFactory(MetroWireSourceDefinition source, PhysicalWireTargetDefinition target) {
    }

    public void attachObjectFactory(MetroWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target) {
        throw new UnsupportedOperationException();
    }

    /**
     * Updates the application classloader with visibility to Meto classes. Metro requires a Metro proxy interface to be visible to the classloader
     * that loaded the SEI class.To enable this, dynamically add the Metro extension classloader as a parent to the classloader that loaded the SEI
     * class if the host supports classloader isolation. Note that the latter may be different than the application classloader (e.g. in the Maven
     * iTest runtime, the SEI class will be loaded by the host classloader, not the classloader representing the application
     *
     * @param seiClass the service interface
     * @return the updated classloader
     */
    private ClassLoader updateClassLoader(Class<?> seiClass) {
        ClassLoader seiClassLoader = seiClass.getClassLoader();
        ClassLoader extensionClassLoader = getClass().getClassLoader();
        if (seiClassLoader instanceof MultiParentClassLoader) {
            MultiParentClassLoader multiParentClassLoader = (MultiParentClassLoader) seiClassLoader;
            if (!multiParentClassLoader.getParents().contains(extensionClassLoader)) {
                multiParentClassLoader.addParent(extensionClassLoader);
            }
        }
        return extensionClassLoader;
    }


}
