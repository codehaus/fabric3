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

import org.fabric3.spi.command.Command;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.generator.GenerationException;

/**
 * Generates commands for provisioning and releasing classloaders on runtimes for a set of contributions being deployed or undeployed.
 *
 * @version $Revision$ $Date$
 */
public interface ClassLoaderCommandGenerator {

    /**
     * Generates classloader provisioning commands for a set of contributions being deployed.
     *
     * @param contributions the required contributions for the deployment, grouped by zone
     * @return the classloader provisioning commands grouped by zone where they are to be provisioned
     * @throws GenerationException if an error occurs during generation
     */
    Map<String, List<Command>> generate(Map<String, List<Contribution>> contributions) throws GenerationException;

    /**
     * Generates classloader release commands for a set of contributions being undeployed.
     *
     * @param contributions the required contributions for the deployment, grouped by zone
     * @return the classloader provisioning commands grouped by zone where they are being undeployed
     * @throws GenerationException if an error occurs during generation
     */
    Map<String, List<Command>> release(Map<String, List<Contribution>> contributions) throws GenerationException;

}
