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

import org.fabric3.spi.command.CommandSet;
import org.fabric3.spi.model.physical.PhysicalChangeSet;

/**
 * A context used during generation of physical definitions
 *
 * @version $Rev$ $Date$
 */
public interface GeneratorContext {

    /**
     * Returns the current changeset.
     *
     * @return the current changeset
     */
    PhysicalChangeSet getPhysicalChangeSet();

    /**
     * Returns the current command set.
     *
     * @return the current command set
     */
    CommandSet getCommandSet();
}
