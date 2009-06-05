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
package org.fabric3.federation.command;

import org.fabric3.spi.command.Command;

/**
 * A command issued by the zone manager on behalf of a participant to synchronize with the domain.
 *
 * @version $Revision$ $Date$
 */
public class ZoneSyncCommand implements Command {
    private static final long serialVersionUID = 6531949467055199113L;
    private String zoneId;
    private String runtimeId;

    public ZoneSyncCommand(String zoneId, String runtimeId) {
        this.zoneId = zoneId;
        this.runtimeId = runtimeId;
    }

    public String getZoneId() {
        return zoneId;
    }

    public String getRuntimeId() {
        return runtimeId;
    }
}