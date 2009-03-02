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

import javax.xml.namespace.QName;

import org.fabric3.spi.command.AbstractCommand;

public class StopContextCommand extends AbstractCommand {
    private static final long serialVersionUID = 6161772793715132968L;

    private final QName deployable;

    public StopContextCommand(int order, QName deployable) {
        super(order);
        this.deployable = deployable;
        assert deployable != null;
    }

    public QName getDeployable() {
        return deployable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StopContextCommand that = (StopContextCommand) o;

        return !(deployable != null ? !deployable.equals(that.deployable) : that.deployable != null);

    }

    @Override
    public int hashCode() {
        return deployable != null ? deployable.hashCode() : 0;
    }
}
