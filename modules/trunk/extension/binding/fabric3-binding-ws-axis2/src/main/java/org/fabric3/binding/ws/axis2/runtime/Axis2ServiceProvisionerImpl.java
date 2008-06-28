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
package org.fabric3.binding.ws.axis2.runtime;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.transport.http.AxisServlet;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.ws.axis2.provision.Axis2WireSourceDefinition;
import org.fabric3.binding.ws.axis2.provision.AxisPolicy;
import org.fabric3.binding.ws.axis2.runtime.config.F3Configurator;
import org.fabric3.binding.ws.axis2.runtime.policy.PolicyApplier;
import org.fabric3.binding.ws.axis2.runtime.servlet.F3AxisServlet;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class Axis2ServiceProvisionerImpl implements Axis2ServiceProvisioner {

    private final ServletHost servletHost;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final PolicyApplier policyApplier;
    private final F3Configurator f3Configurator;
    private ServiceProvisionerMonitor monitor;

    private ConfigurationContext configurationContext;
    private String servicePath = "axis2";

    public Axis2ServiceProvisionerImpl(@Reference ServletHost servletHost,
                                       @Reference ClassLoaderRegistry classLoaderRegistry,
                                       @Reference PolicyApplier policyApplier,
                                       @Reference F3Configurator f3Configurator,
                                       @Monitor ServiceProvisionerMonitor monitor) {
        this.servletHost = servletHost;
        this.classLoaderRegistry = classLoaderRegistry;
        this.policyApplier = policyApplier;
        this.f3Configurator = f3Configurator;
        this.monitor = monitor;
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
     *
     * @throws Exception If unable to create configuration context.
     */
    @Init
    public void start() throws Exception {

        configurationContext = f3Configurator.getConfigurationContext();

        AxisServlet axisServlet = new F3AxisServlet(configurationContext);
        servletHost.registerMapping("/" + servicePath + "/*", axisServlet);
        monitor.extensionStarted();
    }

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

            setMessageReceivers(wire, axisService);

            configurationContext.getAxisConfiguration().addService(axisService);

            applyPolicies(pwsd, axisService);
            monitor.endpointProvisioned("/" + servicePath + "/" + uri);
        } catch (Exception e) {
            throw new WiringException(e);
        }

    }

    private void applyPolicies(Axis2WireSourceDefinition pwsd, AxisService axisService) throws WiringException, AxisFault {

        for (Iterator<?> i = axisService.getOperations(); i.hasNext();) {

            AxisOperation axisOperation = (AxisOperation) i.next();
            String operation = axisOperation.getName().getLocalPart();

            Set<AxisPolicy> policies = pwsd.getPolicies(operation);
            if (policies == null || policies.size() == 0) {
                continue;
            }

            AxisDescription axisDescription = axisOperation;

            for (AxisPolicy axisPolicy : policies) {

                String message = axisPolicy.getMessage();
                String module = axisPolicy.getModule();
                Element opaquePolicy = axisPolicy.getOpaquePolicy();

                AxisModule axisModule = f3Configurator.getModule(module);
                axisOperation.addModule(axisModule.getName());
                axisOperation.engageModule(axisModule);

                if (message != null) {
                    axisDescription = axisOperation.getMessage(message);
                }
                policyApplier.applyPolicy(axisDescription, opaquePolicy);
            }

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
            MessageReceiver messageReceiver = new InOutServiceProxyHandler(invocationChain);
            axisOp.setMessageReceiver(messageReceiver);
        }

    }

}
