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
package org.fabric3.spi.generator;

import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Wire generator for resources.
 *
 * @version $Revision$ $Date$
 */
public interface ResourceWireGenerator<PWTD extends PhysicalWireTargetDefinition, RD extends ResourceDefinition> {

    /**
     * Generate the target wire definition for logical resource.
     *
     * @param logicalResource the resource being wired to
     * @param context         the current generator context
     * @return Source wire definition.
     * @throws GenerationException if there was a problem generating the wire
     */
    PWTD generateWireTargetDefinition(LogicalResource<RD> logicalResource, GeneratorContext context) throws GenerationException;

}
