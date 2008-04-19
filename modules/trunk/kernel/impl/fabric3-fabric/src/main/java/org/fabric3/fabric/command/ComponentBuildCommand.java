/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.fabric.command;

import org.fabric3.spi.command.AbstractCommand;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * A command to instantiate a component on a runtime.
 *
 * @version $Revision$ $Date$
 */
public class ComponentBuildCommand extends AbstractCommand {
    private final PhysicalComponentDefinition definition;

    public ComponentBuildCommand(int order, PhysicalComponentDefinition definition) {
        super(order);
        this.definition = definition;
    }

    public PhysicalComponentDefinition getDefinition() {
        return definition;
    }

    public String toString() {
        return "ComponentBuild: " + definition.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComponentBuildCommand that = (ComponentBuildCommand) o;

        if (definition != null ?
                !definition.equals(that.definition) : that.definition != null) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (definition != null ? definition.hashCode() : 0);
    }
}
