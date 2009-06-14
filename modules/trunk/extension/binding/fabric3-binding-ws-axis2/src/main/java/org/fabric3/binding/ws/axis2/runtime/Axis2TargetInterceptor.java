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
package org.fabric3.binding.ws.axis2.runtime;

import java.net.ConnectException;
import java.security.Principal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.security.auth.Subject;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.WSDL2Constants;
import org.oasisopen.sca.ServiceUnavailableException;

import org.fabric3.binding.ws.axis2.common.Constant;
import org.fabric3.binding.ws.axis2.provision.AxisPolicy;
import org.fabric3.binding.ws.axis2.runtime.config.F3Configurator;
import org.fabric3.binding.ws.axis2.runtime.policy.PolicyApplier;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Revision$ $Date$
 */
public class Axis2TargetInterceptor implements Interceptor {

    private Interceptor next;
    private List<String> endpointUris;
    private final String operation;
    private final Set<AxisPolicy> policies;
    private Map<String, String> operationInfo;
    private Map<String, String> config;
    private final F3Configurator f3Configurator;
    private final PolicyApplier policyApplier;

    private Random random = new Random();
    private AxisService axisService;
    private ClassLoader classLoader;

    public Axis2TargetInterceptor(List<String> endpointUris, 
                                  String operation, 
                                  Set<AxisPolicy> policies, 
                                  Map<String, String> operationInfo,
                                  Map<String, String> config, 
                                  F3Configurator f3Configurator, 
                                  PolicyApplier policyApplier, 
                                  AxisService axisService,
                                  ClassLoader classLoader) {

        this.operation = operation;
        this.endpointUris = endpointUris;
        this.policies = policies;
        this.f3Configurator = f3Configurator;
        this.policyApplier = policyApplier;
        this.operationInfo = operationInfo;
        this.config = config;
        this.axisService = axisService;
        this.classLoader = classLoader;
    }

    public Interceptor getNext() {
        return next;
    }

    public Message invoke(Message msg) {
        
        List<String> failedUris = new LinkedList<String>();

        String endpointUri = getEndpointUri(failedUris);

        Object[] payload = (Object[]) msg.getBody();
        OMElement message = payload == null ? null : (OMElement) payload[0];

        Options options = new Options();
        options.setTo(new EndpointReference(endpointUri));
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);

        Subject subject = msg.getWorkContext().getSubject();
        if (subject != null && !subject.getPrincipals().isEmpty()) {
            Principal primaryPrincipal = subject.getPrincipals().iterator().next();
            options.setUserName(primaryPrincipal.getName());
        }

        applyOperationInfo(options);
        applyConfig(options);

        Thread currentThread = Thread.currentThread();
        ClassLoader oldCl = currentThread.getContextClassLoader();

        try {

            if (classLoader instanceof MultiParentClassLoader) {
                MultiParentClassLoader mpcl = (MultiParentClassLoader) classLoader;
                mpcl.addParent(getClass().getClassLoader());
            }
            currentThread.setContextClassLoader(classLoader);

            ServiceClient sender = new ServiceClient(f3Configurator.getConfigurationContext(), null);
            sender.setOptions(options);
            sender.getOptions().setTimeOutInMilliSeconds(0l);
            applyPolicies(sender, operation);
            
            AxisOperation axisOperation = getAxisOperation(axisService, operation);
            Message ret = new MessageImpl();
                        
            if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(axisOperation.getMessageExchangePattern()) ||
                WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(axisOperation.getMessageExchangePattern())) {
                try {
                    sender.sendRobust(message);
                } catch (AxisFault e) {
                    if (e.getCause() instanceof ConnectException) {
                        throw e; //retry
                    }
                    ret.setBodyWithFault(e.getDetail());
               }
                    
            } else { //Default MEP is IN-OUT for backward compatibility
                Object result = sender.sendReceive(message);
                if (result instanceof Throwable) {
                    ret.setBodyWithFault(result);
                } else {
                    ret.setBody(result);
                }
            }
            failedUris.clear();

            return ret;

        } catch (AxisFault e) {
            return handleFault(msg, endpointUri, e, failedUris);
        } finally {
            currentThread.setContextClassLoader(oldCl);
        }

    }
    
    private AxisOperation getAxisOperation(AxisService axisService, String opName) {
        AxisOperation axisOperation = new OutInAxisOperation();//Default
        
        if(axisService != null) {
            for (Iterator<?> i = axisService.getOperations(); i.hasNext();) {
                AxisOperation axisOp = (AxisOperation) i.next();
                if(axisOp.getName().getLocalPart().equals(opName)) {
                    axisOperation = axisOp;
                    break;
                }
            }
        }
        return axisOperation;
    }
    

    private Message handleFault(Message msg, String endpointUri, AxisFault e, List<String> failedUris) {

        Throwable cause = e.getCause();
        if (cause instanceof ConnectException) {
            failedUris.add(endpointUri);
            if (failedUris.size() != endpointUris.size()) {
                // Retry till all URIs are exhausted
                return invoke(msg);
            }
        }

        SOAPFaultDetail element = e.getFaultDetailElement();
        if (element == null) {
            throw new ServiceUnavailableException("Service fault was: \n" + e + "\n\n", e);
        }

        OMNode child = element.getFirstOMChild();
        if (child == null) {
            throw new ServiceUnavailableException("Service fault was: \n" + e + "\n\n", e);
        }

        throw new ServiceUnavailableException("Service fault was: \n" + child + "\n\n", e);

    }

    private String getEndpointUri(List<String> failedUris) {

        int index = random.nextInt(endpointUris.size());
        String endpointUri = endpointUris.get(index);

        if (failedUris.contains(endpointUri)) {
            endpointUri = getEndpointUri(failedUris);
        }

        return endpointUri;

    }

    private void applyOperationInfo(Options options) {

        String soapAction = "urn:" + operation;// Default

        if (this.operationInfo != null) {
            String soapActionInfo = this.operationInfo.get(Constant.SOAP_ACTION);
            if (soapActionInfo != null) {
                soapAction = soapActionInfo;
            }
        }
        options.setAction(soapAction);

    }

    private void applyConfig(Options options) {

        if (config != null) {
            boolean mtomEnabled = config.get(Constant.CONFIG_ENABLE_MTOM).equalsIgnoreCase(Constant.VALUE_TRUE);
            if (!mtomEnabled) {
                options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_FALSE);
                return;
            }
        }
        // By default MTOM is enabled for backward compatibility.
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);

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
