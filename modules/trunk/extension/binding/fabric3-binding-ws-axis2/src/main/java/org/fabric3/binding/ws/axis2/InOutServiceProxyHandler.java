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

import java.util.Iterator;
import java.net.URI;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;
import org.fabric3.spi.wire.Wire;
import org.fabric3.scdl.Scope;

/**
 * Proxy handler for the invocation.
 *
 * @version $Revision: 1589 $ $Date: 2007-10-25 23:13:37 +0100 (Thu, 25 Oct 2007) $
 */
public class InOutServiceProxyHandler extends AbstractInOutMessageReceiver {

    private final Wire wire;
    private final InvocationChain invocationChain;
    private final URI scopeId;

    /**
     * @param wire Wire which is proxied.
     * @param invocationChain the invocation chain to invoke
     * @param scopeId the id of the composite scope to use
     */
    public InOutServiceProxyHandler(Wire wire, InvocationChain invocationChain, URI scopeId) {
        
        this.wire = wire;
        this.invocationChain = invocationChain;
        this.scopeId = scopeId;
        
    }

    /**
     * @see org.apache.axis2.receivers.AbstractInOutMessageReceiver#invokeBusinessLogic(org.apache.axis2.context.MessageContext, 
     *                                                                                  org.apache.axis2.context.MessageContext)
     */
    @Override
    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {
        
        Interceptor head = invocationChain.getHeadInterceptor();
        OMElement bodyContent = getInBodyContent(inMessage);

        SimpleWorkContext workContext = new SimpleWorkContext();
        workContext.setScopeIdentifier(Scope.COMPOSITE, scopeId);
        Message input = new MessageImpl(new Object[] {bodyContent}, false, workContext, wire);
        
        Message ret = head.invoke(input);
        OMElement resObject = (OMElement) ret.getBody();
        
        SOAPFactory fac = getSOAPFactory(inMessage);

        SOAPEnvelope envelope = fac.getDefaultEnvelope();
        
        envelope.getBody().addChild(resObject);
        
        outMessage.setEnvelope(envelope);
        
    }

    /*
     * Gets the body content of the incoming message.
     */
    private OMElement getInBodyContent(MessageContext inMessage) {
        
        SOAPEnvelope envelope = inMessage.getEnvelope();
        
        OMElement child = null;
        Iterator<?> children = envelope.getChildElements();
        while(children.hasNext()) {
            child = (OMElement) children.next();
            if("Body".equals(child.getLocalName())) {
                break;
            }
        }
        OMElement bodyContent = child.getFirstElement();
        return bodyContent;
        
    }

}
