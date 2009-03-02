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
package org.fabric3.fabric.command;

import java.net.URI;

import org.fabric3.spi.command.AbstractCommand;

/**
 * A command to attach a contribution classloader as an extension to a contribution classloader that provides an extension point.
 *
 * @version $Revision$ $Date$
 */
public class AttachExtensionCommand extends AbstractCommand {
    private static final long serialVersionUID = -5002990071569611217L;
    private URI contribution;
    private URI provider;

    /**
     * Constructor.
     *
     * @param order        the command order
     * @param contribution the contribution URI providing the extension point
     * @param provider     the extension point provider URI
     */
    public AttachExtensionCommand(int order, URI contribution, URI provider) {
        super(order);
        this.contribution = contribution;
        this.provider = provider;
    }

    public URI getContribution() {
        return contribution;
    }

    public URI getProvider() {
        return provider;
    }
}
