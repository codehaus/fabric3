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
package org.fabric3.binding.tcp.scdl;

import java.net.URI;

import javax.xml.namespace.QName;

import org.fabric3.scdl.BindingDefinition;
import org.w3c.dom.Document;

/**
 * Binding definition loaded from the SCDL.
 * 
 * @version $Revision$ $Date$
 */
public class TCPBindingDefinition extends BindingDefinition {
   
    /** The Constant BINDING_QNAME. */
    private static final QName BINDING_QNAME = new QName("urn:org.fabric3:binding:tcp", "binding.tcp");

    private static final long serialVersionUID = -7452725813760060404L;

    /**
     * Initialises the binding type.
     * 
     * @param targetUri Target URI.
     */
    public TCPBindingDefinition(URI targetUri, Document key) {
        super(targetUri, BINDING_QNAME, key);
    }
}
