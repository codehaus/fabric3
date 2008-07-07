/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.spi.generator;

import org.fabric3.spi.command.Command;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Generates a Command that must be applied to a runtime based on changes to a logical component.
 *
 * @version $Rev$ $Date$
 */
public interface CommandGenerator {

    /**
     * Gets the order the command generator should be called in.
     *
     * @return an ascending  value where 0 is first
     */
    int getOrder();

    /**
     * Generates a command based on the contents of a logical component
     *
     * @param logicalComponent the logical component to generate the command from
     * @return the generated command or null if no changes were detected
     * @throws GenerationException if an error occurs during generation
     */
    Command generate(LogicalComponent<?> logicalComponent) throws GenerationException;

}
