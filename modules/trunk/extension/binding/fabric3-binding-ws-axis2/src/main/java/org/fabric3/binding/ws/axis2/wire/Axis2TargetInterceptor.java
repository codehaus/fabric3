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
package org.fabric3.binding.ws.axis2.wire;

import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.fabric3.binding.ws.axis2.config.F3Configurator;
import org.fabric3.binding.ws.axis2.physical.Axis2WireTargetDefinition;
import org.fabric3.binding.ws.axis2.policy.PolicyApplier;
import org.fabric3.binding.ws.axis2.policy.PolicyApplierRegistry;
import org.fabric3.spi.deployer.CompositeClassLoader;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class Axis2TargetInterceptor implements Interceptor {

    private Interceptor next;
    private final EndpointReference epr;
    private final String operation;
    private final Set<Element> policies;
    private final F3Configurator f3Configurator;
    private final PolicyApplierRegistry policyApplierRegistry;
    
    /**
     * Initializes the end point reference.
     * 
     * @param target Target wire source definition.
     * @param operation Operation name.
     */
    public Axis2TargetInterceptor(Axis2WireTargetDefinition target, 
                                  String operation, 
                                  Set<Element> policies, 
                                  F3Configurator f3Configurator,
                                  PolicyApplierRegistry policyApplierRegistry) {
        
        this.operation = operation;
        this.epr = new EndpointReference(target.getUri().toASCIIString());
        this.policies = policies;
        this.f3Configurator = f3Configurator;
        this.policyApplierRegistry = policyApplierRegistry;
        
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#getNext()
     */
    public Interceptor getNext() {
        return next;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#invoke(org.fabric3.spi.wire.Message)
     */
    public Message invoke(Message msg) {
        
        Object[] payload = (Object[]) msg.getBody();
        OMElement message = (OMElement) payload[0];
        
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMElement method = fac.createOMElement(operation, null);
        method.addChild(message);
        
        Options options = new Options();
        options.setTo(epr);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        
        Thread currentThread = Thread.currentThread();
        ClassLoader oldCl = currentThread.getContextClassLoader();
        
        try {
            
            // TODO May be we want to do an MPCL with app cl as well
            CompositeClassLoader systemCl = (CompositeClassLoader) getClass().getClassLoader();
            currentThread.setContextClassLoader(systemCl);
            
            ServiceClient sender = new ServiceClient(f3Configurator.getConfigurationContext(), null);
            sender.setOptions(options);
            
            for (AxisModule axisModule : f3Configurator.getModules()) {
                sender.engageModule(axisModule.getName());
            }
            
            // TODO Need to engage the modules globally
            AxisService axisService = sender.getAxisService();
            for (AxisModule axisModule : f3Configurator.getModules()) {
                axisService.engageModule(axisModule);
            }
            
            applyPolicies(axisService);
            
            OMElement result = sender.sendReceive(method);
            
            Message ret = new MessageImpl();
            ret.setBody(result);
            
            return ret;
            
        } catch (AxisFault e) {
            // TODO Send a fault back
            throw new AssertionError(e);
        } finally {
            currentThread.setContextClassLoader(oldCl);
        }
        
    }

    private void applyPolicies(AxisService axisService) throws AssertionError {
        
        if (policies == null) {
            return;
        }
        
        for (Element policy : policies) {
            QName policyName = new QName(policy.getNamespaceURI(), policy.getNodeName());
            PolicyApplier policyApplier = policyApplierRegistry.getPolicyApplier(policyName);
            if (policyApplier == null) {
                throw new AssertionError("Unknown policy " + policyName);
            }
            policyApplier.applyPolicy(axisService, policy);
        }
        
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#setNext(org.fabric3.spi.wire.Interceptor)
     */
    public void setNext(Interceptor next) {
        this.next = next;
    }

}
