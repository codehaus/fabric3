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
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.ws.api.BindingID;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

import org.fabric3.binding.ws.metro.provision.MetroJavaSourceDefinition;
import org.fabric3.binding.ws.metro.provision.PolicyExpressionMapping;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.binding.ws.metro.runtime.codegen.InterfaceGenerator;
import org.fabric3.binding.ws.metro.runtime.core.EndpointConfiguration;
import org.fabric3.binding.ws.metro.runtime.core.EndpointService;
import org.fabric3.binding.ws.metro.runtime.core.JaxbInvoker;
import org.fabric3.binding.ws.metro.runtime.core.EndpointException;
import org.fabric3.binding.ws.metro.runtime.policy.BindingIdResolver;
import org.fabric3.binding.ws.metro.runtime.policy.FeatureResolver;
import org.fabric3.binding.ws.metro.runtime.policy.GeneratedArtifacts;
import org.fabric3.binding.ws.metro.runtime.policy.PolicyAttachmentException;
import org.fabric3.binding.ws.metro.runtime.policy.WsdlGenerationException;
import org.fabric3.binding.ws.metro.runtime.policy.WsdlGenerator;
import org.fabric3.binding.ws.metro.runtime.policy.WsdlPolicyAttacher;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Source wire attacher that provisions Java-based web service endpoints.
 *
 * @version $Rev$ $Date$
 */
public class MetroJavaSourceWireAttacher extends AbstractMetroSourceWireAttacher<MetroJavaSourceDefinition> {
    private ClassLoaderRegistry classLoaderRegistry;
    private FeatureResolver featureResolver;
    private BindingIdResolver bindingIdResolver;
    private InterfaceGenerator interfaceGenerator;
    private WsdlGenerator wsdlGenerator;
    private WsdlPolicyAttacher policyAttacher;

    public MetroJavaSourceWireAttacher(@Reference ClassLoaderRegistry classLoaderRegistry,
                                       @Reference FeatureResolver featureResolver,
                                       @Reference BindingIdResolver bindingIdResolver,
                                       @Reference InterfaceGenerator interfaceGenerator,
                                       @Reference WsdlGenerator wsdlGenerator,
                                       @Reference WsdlPolicyAttacher policyAttacher,
                                       @Reference EndpointService endpointService) {
        super(endpointService);
        this.classLoaderRegistry = classLoaderRegistry;
        this.featureResolver = featureResolver;
        this.bindingIdResolver = bindingIdResolver;
        this.interfaceGenerator = interfaceGenerator;
        this.wsdlGenerator = wsdlGenerator;
        this.policyAttacher = policyAttacher;
    }

    public void attach(MetroJavaSourceDefinition source, PhysicalTargetDefinition target, Wire wire) throws WiringException {
        try {
            ServiceEndpointDefinition endpointDefinition = source.getEndpointDefinition();
            QName serviceName = endpointDefinition.getServiceName();
            QName portName = endpointDefinition.getPortName();
            URI servicePath = endpointDefinition.getServicePath();
            List<InvocationChain> invocationChains = wire.getInvocationChains();
            URI classLoaderId = source.getClassLoaderId();
            String interfaze = source.getInterface();
            URL wsdlLocation = source.getWsdlLocation();
            List<QName> requestedIntents = source.getIntents();

            ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);

            // load the service interface
            Class<?> seiClass = classLoader.loadClass(interfaze);
            if (WireAttacherHelper.doGeneration(seiClass)) {
                // if the service interface is not annotated, generate an implementing class that is
                seiClass = interfaceGenerator.generateAnnotatedInterface(seiClass, null, null, null, null);
            }
            // update the classloader
            updateClassLoader(seiClass);

            BindingID bindingId;
            WebServiceFeature[] features = featureResolver.getFeatures(requestedIntents);

            File generatedWsdl = null;
            List<File> generatedSchemas = null;

            ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(seiClass.getClassLoader());
                bindingId = bindingIdResolver.resolveBindingId(requestedIntents);
                if (!source.getPolicies().isEmpty() || !source.getMappings().isEmpty()) {
                    // if policy is configured for the endpoint, generate a WSDL with the policy attachments
                    GeneratedArtifacts artifacts = wsdlGenerator.generate(seiClass, serviceName, bindingId, false);
                    generatedWsdl = artifacts.getWsdl();
                    generatedSchemas = artifacts.getSchemas();
                    List<Element> policyExpressions = source.getPolicies();
                    List<PolicyExpressionMapping> mappings = source.getMappings();
                    policyAttacher.attach(generatedWsdl, policyExpressions, mappings);
                    wsdlLocation = generatedWsdl.toURI().toURL();
                }
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }

            JaxbInvoker invoker = new JaxbInvoker(invocationChains);

            // FIXME remove need to decode
            String path = URLDecoder.decode(servicePath.toASCIIString(), "UTF-8");
            EndpointConfiguration configuration = new EndpointConfiguration(seiClass,
                                                                            serviceName,
                                                                            portName,
                                                                            path,
                                                                            wsdlLocation,
                                                                            invoker,
                                                                            features,
                                                                            bindingId,
                                                                            generatedWsdl,
                                                                            generatedSchemas);

            endpointService.registerService(configuration);
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
        } catch (EndpointException e) {
            throw new WiringException(e);
        }
    }

    /**
     * Updates the application classloader with visibility to Meto classes. Metro requires a Metro proxy interface to be visible to the classloader
     * that loaded the SEI class.To enable this, dynamically add the Metro extension classloader as a parent to the classloader that loaded the SEI
     * class if the host supports classloader isolation. Note that the latter may be different than the application classloader (e.g. in the Maven
     * iTest runtime, the SEI class will be loaded by the host classloader, not the classloader representing the application
     *
     * @param seiClass the service interface
     */
    private void updateClassLoader(Class<?> seiClass) {
        ClassLoader seiClassLoader = seiClass.getClassLoader();
        ClassLoader extensionClassLoader = getClass().getClassLoader();
        if (seiClassLoader instanceof MultiParentClassLoader) {
            MultiParentClassLoader multiParentClassLoader = (MultiParentClassLoader) seiClassLoader;
            if (!multiParentClassLoader.getParents().contains(extensionClassLoader)) {
                multiParentClassLoader.addParent(extensionClassLoader);
            }
        }
    }

}
