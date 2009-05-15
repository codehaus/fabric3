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

import javax.jms.JMSException;
import javax.jms.Message;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.binding.format.EncodeCallback;

/**
 * EncodeCallback that populates a JMS Message.
 *
 * @version $Revision$ $Date$
 */
public class JMSEncodeCallback implements EncodeCallback {
    private Message jmsMessage;

    public JMSEncodeCallback(Message jmsMessage) {
        this.jmsMessage = jmsMessage;
    }

    public void encodeContentLengthHeader(long length) {
        // no-op
    }

    public void encodeOperationHeader(String name) {
        try {
            jmsMessage.setStringProperty("scaOperationName", name);
        } catch (JMSException e) {
            // this exception is thrown synchronously so it will be bubbled up to the client
            throw new ServiceRuntimeException(e);
        }
    }

    public void encodeRoutingHeader(String header) {
        try {
            jmsMessage.setStringProperty("f3Context", header);
        } catch (JMSException e) {
            // this exception is thrown synchronously so it will be bubbled up to the client
            throw new ServiceRuntimeException(e);
        }
    }

    public void encodeRoutingHeader(byte[] header) {
        throw new UnsupportedOperationException("byte[] serialization not supported");
    }
}
