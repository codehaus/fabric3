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
package org.fabric3.binding.ftp.scdl;

import java.net.URI;

import org.fabric3.binding.ftp.common.Constants;
import org.fabric3.scdl.BindingDefinition;
import org.w3c.dom.Document;

/**
 * Binding definition loaded from the SCDL.
 * 
 * @version $Revision$ $Date$
 */
public class FtpBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = -889044951554792780L;
    
    private final TransferMode transferMode;

    /**
     * Initializes the binding type.
     * 
     * @param uri Target URI.
     * @param transferMode the FTP transfer mode
     */
    public FtpBindingDefinition(URI uri, TransferMode transferMode, Document key) {
        super(uri, Constants.BINDING_QNAME, key);
        this.transferMode = transferMode;
    }

    /**
     * Gets the transfer mode.
     * 
     * @return File transfer mode.
     */
    public TransferMode getTransferMode() {
        return transferMode;
    }

}
