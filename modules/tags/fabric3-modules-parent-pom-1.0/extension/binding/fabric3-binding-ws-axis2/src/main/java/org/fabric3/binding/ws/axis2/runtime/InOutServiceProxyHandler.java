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
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Implementation of In-Out MEP receiver for Axis2.
 *
 * @version $Revision: 1589 $ $Date: 2007-10-25 23:13:37 +0100 (Thu, 25 Oct 2007) $
 */
public class InOutServiceProxyHandler extends AbstractInOutMessageReceiver {

    private final InvocationChain invocationChain;

    /**
     * @param invocationChain the invocation chain to invoke
     */
    public InOutServiceProxyHandler(InvocationChain invocationChain) {
        this.invocationChain = invocationChain;
    }

    /** 
     * Invoke service implementation with input parameters and send response back to the caller.
     * 
     * @param messageCtx Axis2 message context  
     */
    @Override
    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {

        Interceptor head = invocationChain.getHeadInterceptor();
        OMElement bodyContent = ServiceProxyHelper.getInBodyContent(inMessage);
        Object[] args = bodyContent == null ? null : new Object[]{bodyContent};

        WorkContext workContext = new WorkContext();
        //Attach authenticated Subject to work context
        ServiceProxyHelper.attachSubjectToWorkContext(workContext, inMessage);

        Message input = new MessageImpl(args, false, workContext);

        Message ret = head.invoke(input);

        SOAPFactory fac = getSOAPFactory(inMessage);
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        SOAPBody body = envelope.getBody();

        if (ret.isFault()) {
            Object element = ret.getBody();
            if (element instanceof AxisFault) {
                throw (AxisFault) element;
            } else if (element instanceof Throwable) {
                throw AxisFault.makeFault((Throwable) element);
            }
        } else {
            OMElement resObject = (OMElement) ret.getBody();
            body.addChild(resObject);
        }
        outMessage.setEnvelope(envelope);
    }
}
