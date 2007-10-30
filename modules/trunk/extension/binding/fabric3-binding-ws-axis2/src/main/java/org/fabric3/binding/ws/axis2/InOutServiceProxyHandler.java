/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;
import org.fabric3.spi.wire.Wire;

/**
 * Proxy handler for the invocation.
 *
 * @version $Revision: 1589 $ $Date: 2007-10-25 23:13:37 +0100 (Thu, 25 Oct 2007) $
 */
public class InOutServiceProxyHandler extends AbstractInOutMessageReceiver {

    /**
     * Wire attached to the servlet.
     */
    private Wire wire;

    /**
     * Invocation chain.
     */
    private InvocationChain invocationChain;

    /**
     * @param wire Wire which is proxied.
     */
    public InOutServiceProxyHandler(Wire wire, InvocationChain invocationChain) {
        
        this.wire = wire;
        this.invocationChain = invocationChain;
        
    }

    /**
     * @see org.apache.axis2.receivers.AbstractInOutMessageReceiver#invokeBusinessLogic(org.apache.axis2.context.MessageContext, 
     *                                                                                  org.apache.axis2.context.MessageContext)
     */
    @Override
    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {
        
        AxisOperation op = inMessage.getOperationContext().getAxisOperation();
        AxisService service = inMessage.getAxisService();
        
        Interceptor head = invocationChain.getHeadInterceptor();
        OMElement omElement = inMessage.getEnvelope();
        Message input = new MessageImpl(new Object[] {omElement}, false, new SimpleWorkContext(), wire);
        
        Message ret = head.invoke(input);
        OMElement resObject = (OMElement) ret.getBody();
        
        SOAPFactory fac = getSOAPFactory(inMessage);
        
        // Handling the response
        AxisMessage outaxisMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
        String messageNameSpace = null;
        if (outaxisMessage != null && outaxisMessage.getElementQName() !=null) {
            messageNameSpace = outaxisMessage.getElementQName().getNamespaceURI();
        } else {
            messageNameSpace = service.getTargetNamespace();
        }
        
        String partName = outMessage.getAxisMessage().getPartName();

        OMNamespace ns = fac.createOMNamespace(messageNameSpace, service.getSchemaTargetNamespacePrefix());
        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        OMElement bodyContent = fac.createOMElement(partName, ns);
        
        bodyContent.addChild(resObject);
        envelope.getBody().addChild(bodyContent);
        
        outMessage.setEnvelope(envelope);
        
    }

}
