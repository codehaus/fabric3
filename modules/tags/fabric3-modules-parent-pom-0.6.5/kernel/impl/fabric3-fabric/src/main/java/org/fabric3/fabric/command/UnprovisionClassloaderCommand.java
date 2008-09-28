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

public class UnprovisionClassloaderCommand extends AbstractCommand {

    private final URI uri;
    public UnprovisionClassloaderCommand(int order, URI uri) {
        super(order);
        this.uri = uri;
        assert uri != null;
    }

    public URI getUri() {
        return uri;
    }

    public int hashCode() {
        return uri.hashCode();
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        try {
            UnprovisionClassloaderCommand other = (UnprovisionClassloaderCommand) obj;
            return uri.equals(other.uri);
        } catch (ClassCastException cce) {
            return false;
        }
    }

}

