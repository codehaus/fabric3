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
 * @version $Revision$ $Date$
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
