/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.ws.axis2.runtime;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Implementation of In-Only and In-Robust only MEP receivers for Axis2. Following In-Only MEPs are implemented:
 * 
 * WSDL2Constants.MEP_URI_IN_ONLY - In only message without any fault.
 * WSDL2Constants.MEP_URI_ROBUST_IN_ONLY - In only message with a possible fault. 
 *
 */
public class InOnlyServiceProxyHandler extends AbstractInMessageReceiver {

    private final InvocationChain invocationChain;

    /**
     * @param invocationChain the invocation chain to invoke
     */
    public InOnlyServiceProxyHandler(InvocationChain invocationChain) {
        this.invocationChain = invocationChain;
    }

   
    /**
     * Invoke service implementation with input parameters and send possible fault back to the caller.
     * 
     * @param messageCtx Axis2 message context 
     */
    @Override
    protected void invokeBusinessLogic(MessageContext messageCtx) throws AxisFault {

        Interceptor head = invocationChain.getHeadInterceptor();
        OMElement bodyContent = ServiceProxyHelper.getInBodyContent(messageCtx);
        Object[] args = bodyContent == null ? null : new Object[]{bodyContent};

        WorkContext workContext = new WorkContext();
        //Attach authenticated Subject to work context
        ServiceProxyHelper.attachSubjectToWorkContext(workContext, messageCtx);

        Message input = new MessageImpl(args, false, workContext);

        Message ret = head.invoke(input);
        
        Object element = ret.getBody();
        if (element instanceof AxisFault) {
            throw (AxisFault) element;
            
        } else if (element instanceof Throwable) {
            throw AxisFault.makeFault((Throwable) element);
            
        } else if (element instanceof OMElement) {
            OMElement webFault =  (OMElement)element;
            AxisFault fault = new AxisFault(webFault.getQName().toString());
            fault.setDetail(webFault);            
            throw fault;
        }        
    }
}
