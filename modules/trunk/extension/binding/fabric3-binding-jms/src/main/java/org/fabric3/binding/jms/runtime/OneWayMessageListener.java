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
 * --- Original Apache License ---
 *
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
package org.fabric3.binding.jms.runtime;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.binding.jms.runtime.helper.JmsHelper;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;

/**
 * Listens for one-way requests sent to a destination and dispatches them to a service.
 *
 * @version $Revison$ $Date: 2008-03-18 05:24:49 +0800 (Tue, 18 Mar 2008) $
 */
public class OneWayMessageListener extends AbstractSourceMessageListener {

    public OneWayMessageListener(WireHolder wireHolder) {
        super(wireHolder);
    }

    public void onMessage(Message request, Session responseSession, Destination responseDestination)
            throws JmsOperationException, JmsBadMessageException {
        try {
            String opName = request.getStringProperty(JmsConstants.OPERATION_HEADER);
            InvocationChainHolder holder = getInvocationChainHolder(opName);
            Interceptor interceptor = holder.getChain().getHeadInterceptor();
            PayloadType payloadType = holder.getPayloadType();

            Object payload = MessageHelper.getPayload(request, payloadType);

            switch (payloadType) {

            case OBJECT:
                payload = new Object[]{payload};
                invoke(request, interceptor, payload);
                break;
            case TEXT:
                MessageEncoder messageEncoder = wireHolder.getMessageEncoder();
                if (messageEncoder != null) {
                    decodeAndInvoke(request, opName, interceptor, payload, messageEncoder);
                } else {
                    // non-encoded text
                    payload = new Object[]{payload};
                    invoke(request, interceptor, payload);
                }
                break;
            case STREAM:
                throw new UnsupportedOperationException();
            default:
                payload = new Object[]{payload};
                invoke(request, interceptor, payload);
                break;
            }


        } catch (JMSException ex) {
            throw new Fabric3JmsException("Unable to send response", ex);
        }

    }

    private void decodeAndInvoke(Message request, String opName, Interceptor interceptor, Object payload, MessageEncoder messageEncoder)
            throws JmsOperationException {
        try {
            JMSHeaderContext context = new JMSHeaderContext(request);
            org.fabric3.spi.invocation.Message inMessage = messageEncoder.decode((String) payload, context);
            ParameterEncoder parameterEncoder = wireHolder.getParameterEncoder();
            Object deserialized = parameterEncoder.decode(opName, (String) inMessage.getBody());
            if (deserialized == null) {
                inMessage.setBody(null);
            } else {
                inMessage.setBody(new Object[]{deserialized});
            }
            String callbackUri = wireHolder.getCallbackUri();
            JmsHelper.addCallFrame(inMessage, callbackUri);
            org.fabric3.spi.invocation.Message outMessage = interceptor.invoke(inMessage);
            if (outMessage.isFault()) {
                throw new JmsOperationException((Throwable) outMessage.getBody());
            }
        } catch (EncoderException e) {
            throw new JmsOperationException(e);
        }
    }

    private void invoke(Message request, Interceptor interceptor, Object payload) throws JmsOperationException, JmsBadMessageException {
        WorkContext workContext = JmsHelper.createWorkContext(request, wireHolder.getCallbackUri());
        org.fabric3.spi.invocation.Message inMessage = new MessageImpl(payload, false, workContext);
        org.fabric3.spi.invocation.Message outMessage = interceptor.invoke(inMessage);
        if (outMessage.isFault()) {
            throw new JmsOperationException((Throwable) outMessage.getBody());
        }
    }

}
