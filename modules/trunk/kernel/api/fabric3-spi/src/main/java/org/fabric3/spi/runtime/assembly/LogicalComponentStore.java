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
package org.fabric3.spi.runtime.assembly;

import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Implementations persist the logical SCA domain model
 *
 * @version $Rev$ $Date$
 */
public interface LogicalComponentStore {

    /**
     * Stores the domain model.
     *
     * @param domain the domain model
     * @throws RecordException if an error occurs storing the domain
     */
    void store(LogicalComponent<CompositeImplementation> domain) throws RecordException;

    /**
     * Reads the domain model from the store.
     *
     * @return the domain model from the store
     * @throws RecoveryException f an error occurs reading from the store
     */
    LogicalComponent<CompositeImplementation> read() throws RecoveryException;

}
