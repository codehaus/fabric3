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

package org.fabric3.binding.ws.metro.generator;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.binding.ws.metro.provision.MetroJavaSourceDefinition;
import org.fabric3.binding.ws.metro.provision.MetroTargetDefinition;
import org.fabric3.binding.ws.metro.provision.PolicyExpressionMapping;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.SecurityConfiguration;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.binding.ws.model.WsBindingDefinition;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.policy.EffectivePolicy;

/**
 * Generates MetroSourceDefinitions and MetroTargetDefinitions for a JavaServiceContact.
 *
 * @version $Rev$ $Date$
 */
public class JavaGeneratorDelegate implements MetroGeneratorDelegate<JavaServiceContract> {

    private EndpointResolver endpointResolver;
    private EndpointSynthesizer synthesizer;
    private ClassLoaderRegistry classLoaderRegistry;
    private HostInfo info;

    public JavaGeneratorDelegate(@Reference EndpointResolver endpointResolver,
                                 @Reference EndpointSynthesizer synthesizer,
                                 @Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference HostInfo info) {
        this.endpointResolver = endpointResolver;
        this.synthesizer = synthesizer;
        this.classLoaderRegistry = classLoaderRegistry;
        this.info = info;
    }

    public MetroJavaSourceDefinition generateWireSource(LogicalBinding<WsBindingDefinition> binding,
                                                        JavaServiceContract contract,
                                                        EffectivePolicy policy) throws GenerationException {
        Class<?> serviceClass = loadServiceClass(binding, contract);
        WsBindingDefinition definition = binding.getDefinition();
        URL wsdlLocation = getWsdlLocation(definition, serviceClass);
        ServiceEndpointDefinition endpointDefinition;
        URI targetUri = binding.getDefinition().getTargetUri();
        if (targetUri != null) {
            endpointDefinition = synthesizer.synthesizeServiceEndpoint(contract, serviceClass, targetUri);
        } else {
            // no target uri specified, check wsdlElement
            URI uri = URI.create(binding.getDefinition().getWsdlElement());
            QName deployable = binding.getParent().getParent().getDeployable();
            endpointDefinition = endpointResolver.resolveServiceEndpoint(deployable, uri, wsdlLocation);
        }

        String interfaze = contract.getQualifiedInterfaceName();

        // handle endpoint-level intents provided by Metro
        List<QName> intentNames = new ArrayList<QName>();
        Set<Intent> endpointIntents = policy.getEndpointIntents();
        for (Intent intent : endpointIntents) {
            intentNames.add(intent.getName());
        }

        // handle endpoint-level policies
        List<Element> policyExpressions = new ArrayList<Element>();
        for (PolicySet policySet : policy.getEndpointPolicySets()) {
            policyExpressions.add(policySet.getExpression());
        }

        // Note operation level provided intents are not currently supported. Intents are mapped to JAX-WS features, which are per endpoint.
        List<PolicyExpressionMapping> mappings = GenerationHelper.createMappings(policy, serviceClass);

        return new MetroJavaSourceDefinition(endpointDefinition, interfaze, wsdlLocation, intentNames, policyExpressions, mappings);
    }

    public MetroTargetDefinition generateWireTarget(LogicalBinding<WsBindingDefinition> binding,
                                                    JavaServiceContract contract,
                                                    EffectivePolicy policy) throws GenerationException {
        Class<?> serviceClass = loadServiceClass(binding, contract);
        WsBindingDefinition definition = binding.getDefinition();
        URI targetUri = definition.getTargetUri();
        ReferenceEndpointDefinition endpointDefinition;
        URL wsdlLocation = getWsdlLocation(definition, serviceClass);
        if (targetUri != null) {
            try {
                // TODO get rid of need to decode
                URL url = new URL(URLDecoder.decode(targetUri.toASCIIString(), "UTF-8"));
                endpointDefinition = synthesizer.synthesizeReferenceEndpoint(contract, serviceClass, url);
            } catch (MalformedURLException e) {
                throw new GenerationException(e);
            } catch (UnsupportedEncodingException e) {
                throw new GenerationException(e);
            }
        } else {
            // no target uri specified, introspect from wsdlElement
            URI uri = URI.create(definition.getWsdlElement());
            QName deployable = binding.getParent().getParent().getDeployable();
            endpointDefinition = endpointResolver.resolveReferenceEndpoint(deployable, uri, wsdlLocation);
        }

        String interfaze = contract.getQualifiedInterfaceName();

        Set<Intent> endpointIntents = policy.getEndpointIntents();
        List<QName> intentNames = new ArrayList<QName>();
        for (Intent intent : endpointIntents) {
            intentNames.add(intent.getName());
        }

        // handle endpoint-level policies
        List<Element> policyExpressions = new ArrayList<Element>();
        for (PolicySet policySet : policy.getEndpointPolicySets()) {
            policyExpressions.add(policySet.getExpression());
        }

        // Note operation level provided intents are not currently supported. Intents are mapped to JAX-WS features, which are per endpoint.

        // map operation-level policies
        List<PolicyExpressionMapping> mappings = GenerationHelper.createMappings(policy, serviceClass);

        // obtain security information
        SecurityConfiguration securityConfiguration = GenerationHelper.createSecurityConfiguration(definition);

        // obtain connection information
        ConnectionConfiguration connectionConfiguration = GenerationHelper.createConnectionConfiguration(definition);

        return new MetroTargetDefinition(endpointDefinition,
                                         wsdlLocation,
                                         interfaze,
                                         intentNames,
                                         policyExpressions,
                                         mappings,
                                         securityConfiguration,
                                         connectionConfiguration);
    }

    /**
     * Returns the WSDL location if one is defined in the binding configuration or null.
     *
     * @param definition   the binding configuration
     * @param serviceClass the service endpoint interface
     * @return the WSDL location or null
     * @throws GenerationException if the WSDL location is invalid
     */
    private URL getWsdlLocation(WsBindingDefinition definition, Class<?> serviceClass) throws GenerationException {
        try {
            String location = definition.getWsdlLocation();
            if (location != null) {
                return new URL(location);
            }
            WebService annotation = serviceClass.getAnnotation(WebService.class);
            if (annotation != null) {
                String wsdlLocation = annotation.wsdlLocation();
                if (wsdlLocation.length() > 0) {
                    return new URL(wsdlLocation);
                } else {
                    return null;
                }
            }
        } catch (MalformedURLException e) {
            throw new GenerationException(e);
        }
        return null;

    }

    /**
     * Loads a service contract class in either a host environment that supports classloader isolation or one that does not, in which case the TCCL is
     * used.
     *
     * @param binding      the binding defintion
     * @param javaContract the contract
     * @return the loaded class
     */
    private Class<?> loadServiceClass(LogicalBinding<WsBindingDefinition> binding, JavaServiceContract javaContract) {
        ClassLoader loader;
        if (info.supportsClassLoaderIsolation()) {
            URI classLoaderUri = binding.getParent().getParent().getDefinition().getContributionUri();
            // check if a namespace is assigned
            loader = classLoaderRegistry.getClassLoader(classLoaderUri);
            if (loader == null) {
                // programming error
                throw new AssertionError("Classloader not found: " + classLoaderUri);
            }
        } else {
            loader = Thread.currentThread().getContextClassLoader();
        }
        Class<?> clazz;
        try {
            clazz = loader.loadClass(javaContract.getInterfaceClass());
        } catch (ClassNotFoundException e) {
            // programming error
            throw new AssertionError(e);
        }
        return clazz;
    }


}
