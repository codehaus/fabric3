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
package org.fabric3.fabric.generator.classloader;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.spi.command.Command;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Generates commands for provisioning and releasing classloaders on runtimes for a set of components being deployed or undeployed.
 *
 * @version $Revision$ $Date$
 */
public interface ClassLoaderCommandGenerator {

    /**
     * Generates classloader provisioning commands for a set of components being deployed.
     *
     * @param components the components being deployed
     * @return the classloader provisioning commands grouped by zone where they are to be provisioned
     * @throws GenerationException if an error occurs during generation
     */
    Map<String, Set<Command>> generate(List<LogicalComponent<?>> components) throws GenerationException;

    /**
     * Generates classloader release commands for a set of components being undeployed.
     *
     * @param components the components being undeployed
     * @return the classloader provisioning commands grouped by zone where they are being undeployed
     * @throws GenerationException if an error occurs during generation
     */
    Map<String, Set<Command>> release(List<LogicalComponent<?>> components) throws GenerationException;

}
