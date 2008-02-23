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

import java.net.URI;
import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import javax.security.auth.Subject;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.receivers.AbstractInOutMessageReceiver;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.util.WSSecurityUtil;

import org.fabric3.scdl.Scope;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;

/**
 * Proxy handler for the invocation.
 *
 * @version $Revision: 1589 $ $Date: 2007-10-25 23:13:37 +0100 (Thu, 25 Oct 2007) $
 */
public class InOutServiceProxyHandler extends AbstractInOutMessageReceiver {

    private final InvocationChain invocationChain;
    private final URI scopeId;

    /**
     * @param invocationChain the invocation chain to invoke
     * @param scopeId         the id of the composite scope to use
     */
    public InOutServiceProxyHandler(InvocationChain invocationChain, URI scopeId) {
        this.invocationChain = invocationChain;
        this.scopeId = scopeId;
    }

    @Override
    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {

        Interceptor head = invocationChain.getHeadInterceptor();
        OMElement bodyContent = getInBodyContent(inMessage);

        WorkContext workContext = new WorkContext();
        //Attach authenticated Subject to work context
        attachSubjectToWorkContext(workContext, inMessage);

        Message input = new MessageImpl(new Object[]{bodyContent}, false, workContext);

        Message ret = head.invoke(input);
        OMElement resObject = (OMElement) ret.getBody();

        SOAPFactory fac = getSOAPFactory(inMessage);

        SOAPEnvelope envelope = fac.getDefaultEnvelope();

        envelope.getBody().addChild(resObject);

        outMessage.setEnvelope(envelope);

    }

    /**
     * Attaches the Security principal found after axis2/wss4j security processing to work context.
     *
     * @param workContext f3 work context
     * @param inMessage   In coming axis2 message context
     * @see org.apache.ws.security.processor.SignatureProcessor
     * @see org.apache.rampart.handler.RampartReceiver
     */
    private void attachSubjectToWorkContext(WorkContext workContext, MessageContext inMessage) {
        Vector<WSHandlerResult> wsHandlerResults = (Vector<WSHandlerResult>) inMessage.getProperty(WSHandlerConstants.RECV_RESULTS);

        // Iterate over principals
        if ((wsHandlerResults != null) && (wsHandlerResults.size() > 0)) {
            HashSet<Principal> principals = new HashSet<Principal>();

            for (WSHandlerResult wsHandlerResult : wsHandlerResults) {//Iterate through all wsHandler results to find Principals
                Principal foundPrincipal = null;
                WSSecurityEngineResult signResult = WSSecurityUtil.fetchActionResult(wsHandlerResult.getResults(), WSConstants.UT);
                if (signResult == null) {
                    signResult = WSSecurityUtil.fetchActionResult(wsHandlerResult.getResults(), WSConstants.SIGN);
                }

                if (signResult != null) {
                    foundPrincipal = (Principal) signResult.get(WSSecurityEngineResult.TAG_PRINCIPAL);
                }

                //Create Subject with principal found
                if (foundPrincipal != null) {
                    principals.add(foundPrincipal);
                    ;
                }
            }

            if (principals.size() > 0) {// If we have found principals then set newly created Subject on work context
                workContext.setSubject(new Subject(false, principals, new HashSet<Principal>(), new HashSet<Principal>()));
            }
        }
    }

    /*
     * Gets the body content of the incoming message.
     */
    private OMElement getInBodyContent(MessageContext inMessage) {

        SOAPEnvelope envelope = inMessage.getEnvelope();

        OMElement child = null;
        Iterator<?> children = envelope.getChildElements();
        while (children.hasNext()) {
            child = (OMElement) children.next();
            if ("Body".equals(child.getLocalName())) {
                break;
            }
        }
        OMElement bodyContent = child.getFirstElement();
        return bodyContent;

    }

}
