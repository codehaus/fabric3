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

/**
 * Defines JMS constants.
 *
 * @version $Revision$ $Date$
 */
public interface JmsConstants {

    /**
     * Header used to specify the service operation name being invoked.
     */
    String OPERATION_HEADER = "scaOperationName";

    /**
     * Header used to send routing (callback and conversation) information.
     */
    String ROUTING_HEADER = "f3Context";

    /**
     * Header used to determine if a response is a fault.
     */
    String FAULT_HEADER = "f3Fault";

    /**
     * Identifies the default configured non-XA connection factory
     */
    String DEFAULT_CONNECTION_FACTORY = "default";

    /**
     * Identifies the default configured XA-enabled connection factory
     */
    String DEFAULT_XA_CONNECTION_FACTORY = "xaDefault";

}
