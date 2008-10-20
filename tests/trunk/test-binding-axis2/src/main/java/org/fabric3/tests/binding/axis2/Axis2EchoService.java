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
package org.fabric3.tests.binding.axis2;

import javax.jws.WebMethod;

import org.apache.axiom.om.OMElement;

/**
 * @version $Rev$ $Date$
 */
public interface Axis2EchoService {
    /**
     * Web service operation used to -
     * <ol> 
     *  <li>demonstrate UsernameToken WS Security
     *  <li> demonstrate custom configuration of soap-action
     * </ol>
     * 
     * @param message request payload
     * @return response payload
     */
    @WebMethod(action="echoWs")
    OMElement echoWsUsernameToken(OMElement message);
    
    /**
     * Web service operation used to demonstrate X509Token WS Security
     * 
     * @param message request axiom payload
     * @return response axiom payload
     */
    OMElement echoWsX509Token(OMElement message);
    
    /**
     * Web service operation without any WSS security
     * 
     * @param message request axiom payload
     * @return response axiom payload
     */
    OMElement echoNoSecurity(OMElement message);
}
