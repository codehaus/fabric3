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
package org.fabric3.spi.generator;

import org.fabric3.model.type.component.ResourceDefinition;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Wire generator for resources.
 *
 * @version $Revision$ $Date$
 */
public interface ResourceWireGenerator<RD extends ResourceDefinition> {

    /**
     * Generate the target wire definition for logical resource.
     *
     * @param logicalResource the resource being wired to
     * @return Source wire definition.
     * @throws GenerationException if there was a problem generating the wire
     */
    PhysicalWireTargetDefinition generateWireTargetDefinition(LogicalResource<RD> logicalResource) throws GenerationException;

}
