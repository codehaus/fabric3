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
package org.fabric3.binding.ws.axis2.runtime;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
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
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.MessageReceiver;
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
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.services.expression.ExpressionExpander;
import org.fabric3.spi.services.expression.ExpressionExpansionException;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Axis2 Service provisioner.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class Axis2ServiceProvisionerImpl implements Axis2ServiceProvisioner {

    private final ServletHost servletHost;
    private final ClassLoaderRegistry classLoaderRegistry;
    private final PolicyApplier policyApplier;
    private final F3Configurator f3Configurator;
    private ExpressionExpander expander;
    private ServiceProvisionerMonitor monitor;

    private ConfigurationContext configurationContext;
    private String servicePath = "axis2";
    private F3AxisServlet axisServlet;

    public Axis2ServiceProvisionerImpl(@Reference ServletHost servletHost,
                                       @Reference ClassLoaderRegistry classLoaderRegistry,
                                       @Reference PolicyApplier policyApplier,
                                       @Reference F3Configurator f3Configurator,
                                       @Reference ExpressionExpander expander,
                                       @Monitor ServiceProvisionerMonitor monitor) {
        this.servletHost = servletHost;
        this.classLoaderRegistry = classLoaderRegistry;
        this.policyApplier = policyApplier;
        this.f3Configurator = f3Configurator;
        this.expander = expander;
        this.monitor = monitor;
    }

    /**
     * TODO Make configurable: FABRICTHREE-276
     *
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

        axisServlet = new F3AxisServlet(configurationContext);
        String mapping = "/" + servicePath + "/*";
        if (servletHost.isMappingRegistered(mapping)) {
            // wire reprovisioned
            servletHost.unregisterMapping(mapping);
        }
        servletHost.registerMapping(mapping, axisServlet);
        monitor.extensionStarted();
    }

    public void provision(Axis2WireSourceDefinition pwsd, Wire wire) throws WiringException {

        try {

            String uri = expandUri(pwsd.getUri());
            URI classLoaderUri = pwsd.getClassLoaderId();
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
            // Reset the name
            axisService.setName(uri);

            configurationContext.getAxisConfiguration().addService(axisService);

            applyPolicies(pwsd, axisService);

            axisServlet.registerClassLoader("/" + servicePath + "/" + uri, classLoader);
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
        for (InvocationChain chain : wire.getInvocationChains()) {
            interceptors.put(chain.getPhysicalOperation().getName(), chain);
        }

        Utils.fillAxisService(axisService, configurationContext.getAxisConfiguration(), null, null);

        for (Iterator<?> i = axisService.getOperations(); i.hasNext();) {
            AxisOperation axisOp = (AxisOperation) i.next();
            InvocationChain invocationChain = interceptors.get(axisOp.getName().getLocalPart());

            MessageReceiver messageReceiver = null;
            if (WSDL2Constants.MEP_URI_IN_ONLY.equals(axisOp.getMessageExchangePattern()) ||
                    WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(axisOp.getMessageExchangePattern())) {
                messageReceiver = new InOnlyServiceProxyHandler(invocationChain);
            } else {//Default MEP is IN-OUT for backward compatibility
                messageReceiver = new InOutServiceProxyHandler(invocationChain);
            }
            axisOp.setMessageReceiver(messageReceiver);
        }
    }


    /**
     * Expands the target URI if it contains an expression of the form ${..}.
     *
     * @param uri the target uri to expand
     * @return the expanded URI with sourced values for any expressions
     * @throws WiringException if there is an error expanding an expression
     */
    private String expandUri(URI uri) throws WiringException {
        try {
            String decoded = URLDecoder.decode(uri.getPath(), "UTF-8");
            return expander.expand(decoded);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        } catch (ExpressionExpansionException e) {
            throw new WiringException(e);
        }
    }

}
