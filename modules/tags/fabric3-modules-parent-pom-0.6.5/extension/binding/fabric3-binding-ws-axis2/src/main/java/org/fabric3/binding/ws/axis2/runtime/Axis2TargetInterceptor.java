/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.ws.axis2.runtime;

import java.util.Map;
import java.util.Set;

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
import org.fabric3.binding.ws.axis2.common.Constant;
import org.fabric3.binding.ws.axis2.provision.AxisPolicy;
import org.fabric3.binding.ws.axis2.runtime.config.F3Configurator;
import org.fabric3.binding.ws.axis2.runtime.policy.PolicyApplier;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;
import org.osoa.sca.ServiceUnavailableException;

/**
 * @version $Revision$ $Date$
 */
public class Axis2TargetInterceptor implements Interceptor {

    private Interceptor next;
    private final EndpointReference epr;
    private final String operation;
    private final Set<AxisPolicy> policies;
    private Map<String, String> operationInfo;
    private Map<String, String> config;
    private final F3Configurator f3Configurator;
    private final PolicyApplier policyApplier;

    /**
     * Initializes the end point reference.
     *
     * @param endpointUri    the endpoint uri.
     * @param operation      Operation name.
     * @param policies       the set of policies applied to the service or reference configuration
     * @param f3Configurator a configuration helper for classloading
     * @param policyApplier  the helper for applying configured policies
     */
    public Axis2TargetInterceptor(String endpointUri,
                                  String operation,
                                  Set<AxisPolicy> policies,
                                  Map<String, String> operationInfo,
                                  Map<String, String> config,
                                  F3Configurator f3Configurator,
                                  PolicyApplier policyApplier) {

        this.operation = operation;
        this.epr = new EndpointReference(endpointUri);
        this.policies = policies;
        this.f3Configurator = f3Configurator;
        this.policyApplier = policyApplier;
        this.operationInfo = operationInfo;
        this.config = config;
    }

    public Interceptor getNext() {
        return next;
    }

    public Message invoke(Message msg) {

        Object[] payload = (Object[]) msg.getBody();
        OMElement message = payload == null ? null : (OMElement) payload[0];

        Options options = new Options();
        options.setTo(epr);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
                
        applyOperationInfo(options);
        applyConfig(options);
        

        Thread currentThread = Thread.currentThread();
        ClassLoader oldCl = currentThread.getContextClassLoader();

        try {
            // The extension classloader is a temporary workaround for Axis2 security. The security provider is installed in a separate extension
            // contribution which is loaded in a child classloader of the Axis2 extensin (i.e. it imports the Axis2 extension). Axis2 expects the
            // security callback class to be visible from the TCCL. The extension classloader is the classloader that loaded the security
            // contribution and hence has both the security and Axis2 classes visible to it.

            currentThread.setContextClassLoader(f3Configurator.getExtensionClassLoader());

            ServiceClient sender = new ServiceClient(f3Configurator.getConfigurationContext(), null);
            sender.setOptions(options);
            sender.getOptions().setTimeOutInMilliSeconds(0l);
            applyPolicies(sender, operation);

            Object result = sender.sendReceive(message);

            Message ret = new MessageImpl();
            if (result instanceof Throwable) {
                ret.setBodyWithFault(result);
            } else {
                ret.setBody(result);
            }
            return ret;

        } catch (AxisFault e) {
            SOAPFaultDetail element = e.getFaultDetailElement();
            if (element == null) {
                throw new ServiceUnavailableException("Service fault was: \n" + e + "\n\n", e);
            }
            OMNode child = element.getFirstOMChild();
            if (child == null) {
                throw new ServiceUnavailableException("Service fault was: \n" + e + "\n\n", e);
            }
            throw new ServiceUnavailableException("Service fault was: \n" + child + "\n\n", e);
        } finally {
            currentThread.setContextClassLoader(oldCl);
        }

    }    

    private void applyOperationInfo(Options options) {
    	String soapAction = "urn:" + operation;//Default
    	
    	if(this.operationInfo != null) {
    	    String soapActionInfo = this.operationInfo.get(Constant.SOAP_ACTION);
    	    if(soapActionInfo != null) {
    	        soapAction = soapActionInfo;
    	    }
    	}
    	options.setAction(soapAction);	
    }
    
    private void applyConfig(Options options) {
    	if(config != null) {
    	    boolean mtomEnabled = config.get(Constant.CONFIG_ENABLE_MTOM).equalsIgnoreCase(Constant.VALUE_TRUE);
    	    if(!mtomEnabled) {
    	        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_FALSE);
    	        return;
    	    }
    	}
    	//By default MTOM is enabled for backward compatibility.
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
