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

public class StopCompositeContextCommand extends AbstractCommand {
    private static final long serialVersionUID = 6161772793715132968L;

    private final URI groupId;

    public StopCompositeContextCommand(int order, URI groupId) {
        super(order);
        this.groupId = groupId;
        assert groupId != null;
    }

    public URI getGroupId() {
        return groupId;
    }

    public int hashCode() {
        return groupId.hashCode();
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        try {
            StopCompositeContextCommand other = (StopCompositeContextCommand) object;
            return groupId.equals(other.groupId);
        } catch (ClassCastException cce) {
            return false;
        }
    }
}
