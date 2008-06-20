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

import java.util.Set;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;

import org.fabric3.binding.ws.axis2.provision.Axis2WireTargetDefinition;
import org.fabric3.binding.ws.axis2.provision.AxisPolicy;
import org.fabric3.binding.ws.axis2.runtime.config.F3Configurator;
import org.fabric3.binding.ws.axis2.runtime.policy.PolicyApplier;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationRuntimeException;

/**
 * @version $Revision$ $Date$
 */
public class Axis2TargetInterceptor implements Interceptor {

    private Interceptor next;
    private final EndpointReference epr;
    private final String operation;
    private final Set<AxisPolicy> policies;
    private final F3Configurator f3Configurator;
    private final PolicyApplier policyApplier;

    /**
     * Initializes the end point reference.
     *
     * @param target    Target wire source definition.
     * @param operation Operation name.
     */
    public Axis2TargetInterceptor(Axis2WireTargetDefinition target,
                                  String operation,
                                  Set<AxisPolicy> policies,
                                  F3Configurator f3Configurator,
                                  PolicyApplier policyApplier) {

        this.operation = operation;
        this.epr = new EndpointReference(target.getUri().toASCIIString());
        this.policies = policies;
        this.f3Configurator = f3Configurator;
        this.policyApplier = policyApplier;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#getNext()
     */
    public Interceptor getNext() {
        return next;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#invoke(org.fabric3.spi.invocation.Message)
     */
    public Message invoke(Message msg) {

        Object[] payload = (Object[]) msg.getBody();
        OMElement message = payload == null ? null : (OMElement) payload[0];

        Options options = new Options();
        options.setTo(epr);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setAction("urn:" + operation);

        Thread currentThread = Thread.currentThread();
        ClassLoader oldCl = currentThread.getContextClassLoader();

        try {
            // Yhe extension classloader is a temporary workaround for Axis2 security. The security provider is installed in a separate extension
            // contribution which is loaded in a child classloader of the Axis2 extensin (i.e. it imports the Axis2 extension). Axis2 expects the
            // security callback class to be visible from the TCCL. The extension classloader is the classloader that loaded the security
            // contribution and hence has both the security and Axis2 classes visible to it.

            currentThread.setContextClassLoader(f3Configurator.getExtensionClassLoader());

            ServiceClient sender = new ServiceClient(f3Configurator.getConfigurationContext(), null);
            sender.setOptions(options);
            sender.getOptions().setTimeOutInMilliSeconds(0l);
            applyPolicies(sender, operation);

            OMElement result = sender.sendReceive(message);

            Message ret = new MessageImpl();
            // set the result as a normal response even if its an application fault
            ret.setBody(result);

            return ret;

        } catch (AxisFault e) {
            throw new InvocationRuntimeException(e);
        } finally {
            currentThread.setContextClassLoader(oldCl);
        }

    }

    private void applyPolicies(ServiceClient sender, String operation) throws AxisFault {

        if (policies == null) {
            return;
        }

        AxisService axisService = sender.getAxisService();
        AxisOperation axisOperation = axisService.getOperationBySOAPAction("urn:" + operation);
        if (axisOperation == null) {
            axisOperation = axisService.getOperation(ServiceClient.ANON_OUT_IN_OP);
        }
        AxisDescription axisDescription = axisOperation;

        for (AxisPolicy policy : policies) {

            String moduleName = policy.getModule();
            String message = policy.getMessage();

            AxisModule axisModule = f3Configurator.getModule(moduleName);
            axisOperation.addModule(axisModule.getName());
            axisOperation.engageModule(axisModule);

            if (message != null) {
                axisDescription = axisOperation.getMessage(message);
            }

            policyApplier.applyPolicy(axisDescription, policy.getOpaquePolicy());
        }

    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

}
