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

import java.util.LinkedHashSet;
import java.util.Set;

import org.fabric3.spi.command.AbstractCommand;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 *
 * @version $Revision$ $Date$
 */
public class ComponentBuildCommand extends AbstractCommand {

    private final Set<PhysicalComponentDefinition> physicalComponentDefinitions = new LinkedHashSet<PhysicalComponentDefinition>();

    public ComponentBuildCommand(int order) {
        super(order);
    }

    public Set<PhysicalComponentDefinition> getPhysicalComponentDefinitions() {
        return physicalComponentDefinitions;
    }
    
    public void addPhysicalComponentDefinition(PhysicalComponentDefinition physicalComponentDefinition) {
        physicalComponentDefinitions.add(physicalComponentDefinition);
    }
    
    public void addPhysicalComponentDefinitions(Set<PhysicalComponentDefinition> physicalComponentDefinitions) {
        this.physicalComponentDefinitions.addAll(physicalComponentDefinitions);
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (obj == null || obj.getClass() != ComponentBuildCommand.class) {
            return false;
        }
        
        ComponentBuildCommand other = (ComponentBuildCommand) obj;
        return physicalComponentDefinitions.equals(other.physicalComponentDefinitions);
        
    }

    @Override
    public int hashCode() {
        return physicalComponentDefinitions.hashCode();
    }

}
