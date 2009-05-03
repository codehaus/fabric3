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
import org.fabric3.spi.services.serializer.Serializer;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Holder for invocation chains, associated serializers and payload type metadata. Used when dispatching messages.
 *
 * @version $Revision$ $Date$
 */
public class InvocationChainHolder {
    private InvocationChain chain;
    private Serializer inputSerializer;
    private Serializer outputSerializer;
    private PayloadType payloadType;

    public InvocationChainHolder(InvocationChain chain, Serializer inputSerializer, Serializer outputSerializer, PayloadType payloadType) {
        this.chain = chain;
        this.inputSerializer = inputSerializer;
        this.outputSerializer = outputSerializer;
        this.payloadType = payloadType;
    }

    public InvocationChain getChain() {
        return chain;
    }

    public Serializer getInputSerializer() {
        return inputSerializer;
    }

    public Serializer getOutputSerializer() {
        return outputSerializer;
    }

    public PayloadType getPayloadType() {
        return payloadType;
    }
}