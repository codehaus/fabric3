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

import org.fabric3.spi.command.Command;

public class StopComponentCommand implements Command {
    private static final long serialVersionUID = 4385799180032870689L;

    private final URI uri;

    public StopComponentCommand(URI uri) {
        this.uri = uri;
        assert uri != null;
    }

    public URI getUri() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StopComponentCommand that = (StopComponentCommand) o;

        return !(uri != null ? !uri.equals(that.uri) : that.uri != null);

    }

    @Override
    public int hashCode() {
        return uri != null ? uri.hashCode() : 0;
    }
}
