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
package org.fabric3.fabric.generator.classloader;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.spi.command.Command;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Generates classloader definitions for a set of logical components that are to be provisioned to a runtime.
 * <p/>
 * On the runtime, a builder is responsible for matching the PhysicalClassLoaderDefinition to an existing classloader or creating a new one. During
 * this process, the contribution archive and required extensions may need to be provisioned to the runtime.
 * <p/>
 *
 * @version $Revision$ $Date$
 */
public interface ClassLoaderCommandGenerator {

    /**
     * Generates the classloader definitons for a set of logical components.
     *
     * @param components the logical components
     * @return the classloader provisioning commands grouped by runtime id where they are to be provisioned
     * @throws GenerationException if an error occurs during generation
     */
    Map<URI, Set<Command>> generate(List<LogicalComponent<?>> components) throws GenerationException;

}
