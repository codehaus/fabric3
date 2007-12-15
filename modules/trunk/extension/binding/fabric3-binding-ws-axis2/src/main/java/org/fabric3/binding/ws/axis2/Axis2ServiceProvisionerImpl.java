/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.binding.ws.axis2;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.transport.http.AxisServlet;
import org.fabric3.binding.ws.axis2.physical.Axis2WireSourceDefinition;
import org.fabric3.binding.ws.axis2.policy.Axis2PolicyBuilder;
import org.fabric3.binding.ws.axis2.servlet.F3AxisServlet;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class Axis2ServiceProvisionerImpl implements Axis2ServiceProvisioner {
    
    private final ServletHost servletHost;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final Axis2PolicyBuilder policyBuilder;
    
    private ConfigurationContext configurationContext;
    private String servicePath = "axis2";
    
    public Axis2ServiceProvisionerImpl(@Reference ServletHost servletHost,
                                   @Reference ClassLoaderRegistry classLoaderRegistry,
                                   @Reference Axis2PolicyBuilder policyBuilder) {
        this.servletHost = servletHost;
        this.classLoaderRegistry = classLoaderRegistry;
        this.policyBuilder = policyBuilder;
    }
    
    /**
     * @param servicePath Service path for Axis requests.
     */
    @Property
    public void setServicePath(String servicePath) {
        this.servicePath = servicePath;
    }
    
    /**
     * Initializes the servlet mapping.
     * @throws Exception If unable to create configuration context.
     */
    @Init
    public void start() throws Exception {
        
        configurationContext = ConfigurationContextFactory.createDefaultConfigurationContext();
        configurationContext.setServicePath(servicePath);
        
        AxisServlet axisServlet = new F3AxisServlet(configurationContext);
        servletHost.registerMapping("/" + servicePath + "/*", axisServlet);
        
    }

    /**
     * @see org.fabric3.binding.ws.axis2.Axis2ServiceProvisioner#provision(org.fabric3.binding.ws.axis2.physical.Axis2WireSourceDefinition, 
     *                                                                     org.fabric3.spi.wire.Wire)
     */
    public void provision(Axis2WireSourceDefinition pwsd, Wire wire) throws WiringException {
        
        try {
            
            String uri = pwsd.getUri().getPath();
            URI classLoaderUri = pwsd.getClassloaderURI();
            String serviceClass = pwsd.getServiceInterface();
            
            ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderUri);
            
            AxisService axisService = new AxisService();
            
            axisService.setName(uri);
            axisService.setDocumentation("Fabric3 enabled axis service");
            axisService.setClientSide(false);
            axisService.setClassLoader(classLoader);
            axisService.setEndpointURL(uri);
            
            Parameter interfaceParameter = new Parameter(Constants.SERVICE_CLASS, serviceClass);
            axisService.addParameter(interfaceParameter);
            
            // TODO Need to engage the modules globally
            for (Element policyDefinition : pwsd.getPolicyDefinitions()) {
                axisService.applyPolicy(policyBuilder.buildPolicy(policyDefinition));
            }
            
            setMessageReceivers(wire, axisService);
            
            configurationContext.getAxisConfiguration().addService(axisService);
            
        } catch (Exception e) {
            throw new WiringException(e);
        }
        
    }

    /*
     * Adds the message receivers.
     */
    private void setMessageReceivers(Wire wire, AxisService axisService) throws Exception {
        
        Map<String, InvocationChain> interceptors = new HashMap<String, InvocationChain>();
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            interceptors.put(entry.getKey().getName(), entry.getValue());
        }
        
        Utils.fillAxisService(axisService, configurationContext.getAxisConfiguration(), null, null);
        
        for (Iterator<?> i = axisService.getOperations(); i.hasNext();) {
            AxisOperation axisOp = (AxisOperation) i.next();
            InvocationChain invocationChain = interceptors.get(axisOp.getName().getLocalPart());
            // TODO Select message receiver based on MEP
            MessageReceiver messageReceiver = new InOutServiceProxyHandler(wire, invocationChain);
            axisOp.setMessageReceiver(messageReceiver);
        }
        
    }

}
