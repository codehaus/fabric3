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
package org.fabric3.fabric.generator.extension;

import java.util.List;
import java.util.Map;

import org.fabric3.fabric.generator.GenerationType;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Generates commands to un/provision extensions for a deployment or undeployment.
 *
 * @version $Revision$ $Date$
 */
public interface ExtensionGenerator {

    /**
     * Generates the un/provision commands.
     *
     * @param contributions the contributions being deployed or undeployed
     * @param components    the components being deployed or undeployed
     * @param commandMap    the CommandMap being generated
     * @param type          the generation type being performed
     * @return the provision commands keyed by zone
     * @throws GenerationException if an error occurs generating the commands
     */
    Map<String, Command> generate(Map<String, List<Contribution>> contributions,
                                  List<LogicalComponent<?>> components,
                                  CommandMap commandMap,
                                  GenerationType type) throws GenerationException;
}
