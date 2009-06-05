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

import org.fabric3.spi.binding.format.HeaderContext;

/**
 * @version $Revision$ $Date$
 */
public class JMSHeaderContext implements HeaderContext {
    private Message jmsMessage;

    public JMSHeaderContext(Message jmsMessage) {
        this.jmsMessage = jmsMessage;
    }

    public long getContentLength() {
        throw new UnsupportedOperationException();
    }

    public String getOperationName() {
        try {
            return jmsMessage.getStringProperty("scaOperationName");
        } catch (JMSException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public String getRoutingText() {
        try {
            return jmsMessage.getStringProperty(JmsConstants.ROUTING_HEADER);
        } catch (JMSException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public byte[] getRoutingBytes() {
        throw new UnsupportedOperationException();
    }
}
