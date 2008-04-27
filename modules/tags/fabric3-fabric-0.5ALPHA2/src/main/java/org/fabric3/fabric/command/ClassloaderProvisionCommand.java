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
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;

/**
 * A command to create or update a classloader on a runtime.
 *
 * @version $Revision$ $Date$
 */
public class ClassloaderProvisionCommand extends AbstractCommand {
    private PhysicalClassLoaderDefinition physicalClassLoaderDefinition;

    public ClassloaderProvisionCommand(int order, PhysicalClassLoaderDefinition definition) {
        super(order);
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

        ClassloaderProvisionCommand that = (ClassloaderProvisionCommand) o;

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
