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
package org.fabric3.resource.generator;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.resource.model.SystemSourcedResource;
import org.fabric3.resource.model.SystemSourcedWireTargetDefinition;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.ResourceWireGenerator;
import org.fabric3.spi.model.instance.LogicalResource;

/**
 * @version $Revision$ $Date$
 */
@SuppressWarnings("unchecked")
@EagerInit
public class SystemSourcedResourceWireGenerator implements ResourceWireGenerator<SystemSourcedResource> {

    private static final String SYSTEM_URI = "fabric3://runtime/";

    public SystemSourcedWireTargetDefinition generateWireTargetDefinition(LogicalResource<SystemSourcedResource> logicalResource)
            throws GenerationException {

        SystemSourcedResource resourceDefinition = logicalResource.getResourceDefinition();
        String mappedName = resourceDefinition.getMappedName();

        if (mappedName == null) {
            throw new MappedNameNotFoundException();
        }

        URI targetUri = URI.create(SYSTEM_URI + mappedName);

        SystemSourcedWireTargetDefinition wtd = new SystemSourcedWireTargetDefinition();
        wtd.setOptimizable(true);
        wtd.setUri(targetUri);

        return wtd;

    }

}
