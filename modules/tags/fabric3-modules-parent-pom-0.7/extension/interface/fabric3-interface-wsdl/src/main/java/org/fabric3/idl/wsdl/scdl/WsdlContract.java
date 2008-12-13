/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.idl.wsdl.scdl;

import javax.xml.namespace.QName;

import org.fabric3.model.type.service.ServiceContract;

/**
 * WSDL Service contract.
 * 
 * @version $Revsion$ $Date$
 */
public class WsdlContract extends ServiceContract {
    private static final long serialVersionUID = 8084985972954894699L;

    /**
     * QName for the port type/interface.
     */
    private QName qname;
    
    /**
     * Callback qname.
     */
    private QName callbackQname;

    /**
     * @return QName for the port type/interface.
     */
    public QName getQname() {
        return qname;
    }

    /**
     * @param qname QName for the port type/interface.
     */
    public void setQname(QName qname) {
        this.qname = qname;
    }

    /**
     * @return Callback qname.
     */
    public QName getCallbackQname() {
        return callbackQname;
    }

    /**
     * @param callbackQname Callback qname.
     */
    public void setCallbackQname(QName callbackQname) {
        this.callbackQname = callbackQname;
    }

    public boolean isAssignableFrom(ServiceContract serviceContract) {
        throw new UnsupportedOperationException();
    }

    public String getQualifiedInterfaceName() {
        return qname.toString();
    }
}
