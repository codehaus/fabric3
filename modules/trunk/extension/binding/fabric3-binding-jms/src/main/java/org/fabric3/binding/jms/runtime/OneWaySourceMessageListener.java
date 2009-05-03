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

import org.fabric3.binding.jms.common.Fabric3JmsException;
import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.binding.jms.runtime.helper.JmsHelper;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.services.serializer.SerializationException;
import org.fabric3.spi.services.serializer.Serializer;
import org.fabric3.spi.wire.Interceptor;

/**
 * Message listener for one-way service requests.
 *
 * @version $Revison$ $Date: 2008-03-18 05:24:49 +0800 (Tue, 18 Mar 2008) $
 */
public class OneWaySourceMessageListener extends AbstractSourceMessageListener {

    public OneWaySourceMessageListener(WireHolder wireHolder) {
        super(wireHolder);
    }

    public void onMessage(Message request, Session responseSession, Destination responseDestination) throws JmsOperationException {
        try {
            String opName = request.getStringProperty("scaOperationName");
            InvocationChainHolder holder = getInvocationChainHolder(opName);
            Interceptor interceptor = holder.getChain().getHeadInterceptor();
            PayloadType payloadType = holder.getPayloadType();
            Object payload = MessageHelper.getPayload(request, payloadType);
            if (payloadType != PayloadType.OBJECT) {
                Serializer serializer = holder.getInputSerializer();
                if (serializer != null) {
                    try {
                        payload = serializer.deserialize(Object.class, payload);
                    } catch (SerializationException e) {
                        throw new JmsOperationException(e);
                    }
                }
                payload = new Object[]{payload};
            }

            WorkContext workContext = JmsHelper.createWorkContext(request, wireHolder.getCallbackUri());
            org.fabric3.spi.invocation.Message inMessage = new MessageImpl(payload, false, workContext);
            org.fabric3.spi.invocation.Message outMessage = interceptor.invoke(inMessage);
            if (outMessage.isFault()) {
                throw new JmsOperationException((Throwable) outMessage.getBody());
            }

        } catch (JMSException ex) {
            throw new Fabric3JmsException("Unable to send response", ex);
        }

    }

}
