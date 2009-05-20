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
package org.fabric3.binding.jms.runtime;

import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;

/**
 * Template for configuring a JmsInterceptor. If a JmsResponseMessageListener is not set, the interceptor will be configured to perform one-way
 * invocations.
 *
 * @version $Revision$ $Date$
 */
public class InterceptorConfiguration {
    private String operationName;
    private PayloadType payloadType;
    private MessageEncoder messageEncoder;
    private ParameterEncoder parameterEncoder;
    private WireConfiguration wireConfiguration;

    public InterceptorConfiguration(WireConfiguration wireConfiguration) {
        this.wireConfiguration = wireConfiguration;
    }

    public WireConfiguration getWireConfiguration() {
        return wireConfiguration;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public PayloadType getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(PayloadType payloadType) {
        this.payloadType = payloadType;
    }

    public MessageEncoder getMessageEncoder() {
        return messageEncoder;
    }

    public void setMessageEncoder(MessageEncoder messageEncoder) {
        this.messageEncoder = messageEncoder;
    }

    public ParameterEncoder getParameterEncoder() {
        return parameterEncoder;
    }

    public void setParameterEncoder(ParameterEncoder parameterEncoder) {
        this.parameterEncoder = parameterEncoder;
    }

}
