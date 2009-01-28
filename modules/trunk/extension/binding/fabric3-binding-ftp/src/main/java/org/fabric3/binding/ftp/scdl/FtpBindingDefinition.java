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
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import org.fabric3.binding.ftp.common.Constants;
import org.fabric3.model.type.component.BindingDefinition;

/**
 * Binding definition loaded from the SCDL.
 *
 * @version $Revision$ $Date$
 */
public class FtpBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = -889044951554792780L;

    private final TransferMode transferMode;
    private List<String> commands = new ArrayList<String>();
    private String tmpFileSuffix;

    /**
     * Initializes the binding type.
     *
     * @param uri          Target URI.
     * @param transferMode the FTP transfer mode
     * @param key          the key for mapped bindings
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

    /**
     * Gets the list of commands to execute before a STOR operation, i.e. a service invocation.
     *
     * @return the list of commands to execute before a STOR
     */
    public List<String> getSTORCommands() {
        return commands;
    }


    /**
     * Adds a command to execute before a put.
     *
     * @param command the command
     */
    public void addSTORCommand(String command) {
        commands.add(command);
    }

    /**
     * Gets the temporary file suffix to be used while file in transmission (i.e. during STOR operation).
     * 
     * @return temporary file suffix
     */
    public String getTmpFileSuffix() {
        return tmpFileSuffix;
    }

    /**
     * Sets the temporary file suffix to be used while file in transmission (i.e. during STOR operation).
     * 
     * @param tmpFileSuffix temporary file suffix
     */
    public void setTmpFileSuffix(String tmpFileSuffix) {
        this.tmpFileSuffix = tmpFileSuffix;
    }
}
