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

import org.fabric3.spi.command.Command;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;

/**
 * A command to create or update a classloader on a runtime.
 *
 * @version $Revision$ $Date$
 */
public class ProvisionClassloaderCommand implements Command {
    private static final long serialVersionUID = -5993951083285578380L;

    private PhysicalClassLoaderDefinition physicalClassLoaderDefinition;

    public ProvisionClassloaderCommand(PhysicalClassLoaderDefinition definition) {
        this.physicalClassLoaderDefinition = definition;
    }

    public PhysicalClassLoaderDefinition getClassLoaderDefinition() {
        return physicalClassLoaderDefinition;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProvisionClassloaderCommand that = (ProvisionClassloaderCommand) o;

        if (physicalClassLoaderDefinition != null
                ? !physicalClassLoaderDefinition.equals(that.physicalClassLoaderDefinition) : that.physicalClassLoaderDefinition != null) {

            return false;
        }

        return true;
    }

    public int hashCode() {
        return (physicalClassLoaderDefinition != null ? physicalClassLoaderDefinition.hashCode() : 0);
    }
}
