/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.runtime;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.binding.jms.runtime.helper.MessageHelper;
import org.fabric3.binding.jms.runtime.helper.WorkContextHelper;
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
public class OneWayMessageListener extends AbstractServiceMessageListener {

    public OneWayMessageListener(WireHolder wireHolder) {
        super(wireHolder);
    }

    public void onMessage(Message request, Session responseSession, Destination responseDestination)
            throws JmsServiceException, JmsBadMessageException, JMSException {
        String opName = request.getStringProperty(JmsConstants.OPERATION_HEADER);
        InvocationChainHolder holder = getInvocationChainHolder(opName);
        Interceptor interceptor = holder.getChain().getHeadInterceptor();
        PayloadType payloadType = holder.getPayloadType();

        Object payload = MessageHelper.getPayload(request, payloadType);

        switch (payloadType) {

        case OBJECT:
            if (payload != null && !payload.getClass().isArray()) {
                payload = new Object[]{payload};
            }
            invoke(request, interceptor, payload);
            break;
        case TEXT:
            MessageEncoder messageEncoder = wireHolder.getMessageEncoder();
            if (messageEncoder != null) {
                decodeAndInvoke(request, opName, interceptor, payload, messageEncoder);
            } else {
                // non-encoded text
                if (payload != null) {
                    payload = new Object[]{payload};
                }
                invoke(request, interceptor, payload);
            }
            break;
        case STREAM:
            throw new UnsupportedOperationException();
        default:
            if (payload != null) {
                payload = new Object[]{payload};
            }
            invoke(request, interceptor, payload);
            break;
        }


    }

    private void decodeAndInvoke(Message request, String opName, Interceptor interceptor, Object payload, MessageEncoder messageEncoder)
            throws JmsServiceException, JmsBadMessageException {
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
            WorkContextHelper.addCallFrame(inMessage, callbackUri);
            org.fabric3.spi.invocation.Message outMessage = interceptor.invoke(inMessage);
            if (outMessage.isFault()) {
                throw new JmsServiceException((Throwable) outMessage.getBody());
            }
        } catch (EncoderException e) {
            throw new JmsBadMessageException("Error decoding message", e);
        }
    }

    private void invoke(Message request, Interceptor interceptor, Object payload) throws JmsServiceException, JmsBadMessageException {
        WorkContext workContext = WorkContextHelper.createWorkContext(request, wireHolder.getCallbackUri());
        org.fabric3.spi.invocation.Message inMessage = new MessageImpl(payload, false, workContext);
        org.fabric3.spi.invocation.Message outMessage = interceptor.invoke(inMessage);
        if (outMessage.isFault()) {
            // One-way operations should not throw an exception. Raise as an unexpected exception
            throw new RuntimeException((Throwable) outMessage.getBody());
        }
    }

}
