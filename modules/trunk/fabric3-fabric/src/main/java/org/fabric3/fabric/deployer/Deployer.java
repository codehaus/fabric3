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
package org.fabric3.fabric.deployer;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.component.RegistrationException;
import org.fabric3.spi.model.physical.PhysicalChangeSet;

/**
 * Responsible for deploying a changeset to a runtime instance
 *
 * @version $Rev$ $Date$
 */
public interface Deployer {

    /**
     * Apply the changeset to the runtime
     *
     * @param changeSet the changeset to apply
     * @throws BuilderException      if an exception occurs building runtime artifacts from the changeset
     * @throws RegistrationException if an error occurs registering a component built from the changeset
     */
    void applyChangeSet(PhysicalChangeSet changeSet) throws BuilderException, RegistrationException;
}
