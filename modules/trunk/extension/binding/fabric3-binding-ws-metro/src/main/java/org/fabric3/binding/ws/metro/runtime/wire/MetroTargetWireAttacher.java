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
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.wss.SecurityEnvironment;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

import org.fabric3.binding.ws.metro.provision.MetroWireTargetDefinition;
import org.fabric3.binding.ws.metro.provision.PolicyExpressionMapping;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.SecurityConfiguration;
import org.fabric3.binding.ws.metro.runtime.core.MetroProxyObjectFactory;
import org.fabric3.binding.ws.metro.runtime.core.MetroTargetInterceptor;
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
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Creates invocation chains for invoking a target web service.
 *
 * @version $Rev$ $Date$
 */
public class MetroTargetWireAttacher implements TargetWireAttacher<MetroWireTargetDefinition> {

    private ClassLoaderRegistry registry;
    private FeatureResolver resolver;
    private InterfaceGenerator interfaceGenerator;
    private WsdlGenerator wsdlGenerator;
    private WsdlPolicyAttacher policyAttacher;
    private SecurityEnvironment securityEnvironment;
    private WorkScheduler scheduler;
    private XMLInputFactory xmlInputFactory;


    public MetroTargetWireAttacher(@Reference ClassLoaderRegistry registry,
                                   @Reference FeatureResolver resolver,
                                   @Reference InterfaceGenerator interfaceGenerator,
                                   @Reference WsdlGenerator wsdlGenerator,
                                   @Reference WsdlPolicyAttacher policyAttacher,
                                   @Reference SecurityEnvironment securityEnvironment,
                                   @Reference WorkScheduler scheduler,
                                   @Reference XMLFactory xmlFactory) {
        this.registry = registry;
        this.resolver = resolver;
        this.interfaceGenerator = interfaceGenerator;
        this.wsdlGenerator = wsdlGenerator;
        this.policyAttacher = policyAttacher;
        this.securityEnvironment = securityEnvironment;
        this.scheduler = scheduler;
        this.xmlInputFactory = xmlFactory.newInputFactoryInstance();
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, MetroWireTargetDefinition target, Wire wire) throws WiringException {

        try {
            ReferenceEndpointDefinition endpointDefinition = target.getEndpointDefinition();
            QName serviceName = endpointDefinition.getServiceName();
            String interfaze = target.getInterface();
            URI classLoaderId = source.getClassLoaderId();
            List<QName> requestedIntents = target.getRequestedIntents();
            List<PolicySet> requestedPolicySets = null;

            ClassLoader classLoader = registry.getClassLoader(classLoaderId);
            WebServiceFeature[] features = resolver.getFeatures(requestedIntents, requestedPolicySets);

            Class<?> seiClass = classLoader.loadClass(interfaze);
            if (!seiClass.isAnnotationPresent(WebService.class)) {
                // if the service interface is not annotated, generate an implementing class that is
                seiClass = interfaceGenerator.generateAnnotatedInterface(seiClass, null, null, null, null);
            }

            URL wsdlLocation = target.getWsdlLocation();

            File generatedWsdl = null;
            if (!target.getMappings().isEmpty()) {
                // if policy is configured for the endpoint, generate a WSDL with the policy attachments
                GeneratedArtifacts artifacts = wsdlGenerator.generate(seiClass, serviceName, true);
                generatedWsdl = artifacts.getWsdl();
                for (PolicyExpressionMapping mapping : target.getMappings()) {
                    List<String> names = mapping.getOperationNames();
                    Element expression = mapping.getPolicyExpression();
                    policyAttacher.attach(generatedWsdl, names, expression);
                }
            }

            ObjectFactory<?> proxyFactory = new MetroProxyObjectFactory(endpointDefinition,
                                                                        wsdlLocation,
                                                                        generatedWsdl,
                                                                        seiClass,
                                                                        features,
                                                                        scheduler,
                                                                        securityEnvironment,
                                                                        xmlInputFactory);

            Method[] methods = seiClass.getDeclaredMethods();
            SecurityConfiguration configuration = target.getConfiguration();
            for (InvocationChain chain : wire.getInvocationChains()) {
                Method method = null;
                for (Method meth : methods) {
                    if (chain.getPhysicalOperation().getName().equals(meth.getName())) {
                        method = meth;
                        break;
                    }
                }
                MetroTargetInterceptor targetInterceptor = new MetroTargetInterceptor(proxyFactory, method, configuration);
                chain.addInterceptor(targetInterceptor);
            }
        } catch (ClassNotFoundException e) {
            throw new WiringException(e);
        } catch (WsdlGenerationException e) {
            throw new WiringException(e);
        } catch (PolicyAttachmentException e) {
            throw new WiringException(e);
        }

    }

    public ObjectFactory<?> createObjectFactory(MetroWireTargetDefinition target) throws WiringException {
        return null;
    }

    public void detachFromTarget(PhysicalWireSourceDefinition source, MetroWireTargetDefinition target) throws WiringException {
        // no-op
    }

}
