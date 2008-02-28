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

import org.fabric3.fabric.classloader.PhysicalClassLoaderDefinition;
import org.fabric3.spi.command.AbstractCommand;
import org.fabric3.spi.model.physical.PhysicalResourceContainerDefinition;

/**
 *
 * @version $Revision$ $Date$
 */
public class ClassloaderProvisionCommand extends AbstractCommand {
    
    private final PhysicalResourceContainerDefinition physicalResourceContainerDefinition;

    public ClassloaderProvisionCommand(PhysicalResourceContainerDefinition physicalResourceContainerDefinition, int order) {
        super(order);
        this.physicalResourceContainerDefinition = physicalResourceContainerDefinition;
    }

    public PhysicalResourceContainerDefinition getPhysicalResourceContainerDefinition() {
        return physicalResourceContainerDefinition;
    }
    
    @Override
    public String toString() {
        PhysicalClassLoaderDefinition pcl = (PhysicalClassLoaderDefinition) physicalResourceContainerDefinition;
        return pcl.getUri().toString() + ":" + pcl.getResourceUrls() + ":" + pcl.isUpdate();
    }

}
