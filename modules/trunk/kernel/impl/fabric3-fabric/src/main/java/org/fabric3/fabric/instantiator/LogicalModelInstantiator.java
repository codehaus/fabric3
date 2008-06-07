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
package org.fabric3.fabric.instantiator;

import org.fabric3.scdl.Composite;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Interface that abstracts the concerns of instantiating and maintaining logical components within a domain.
 *
 * @version $Revision$ $Date$
 */
public interface LogicalModelInstantiator {

    /**
     * Include the composite into the domain.
     *
     * @param domain    Domain in which the composite is to be included.
     * @param composite Composite to be included in the domain.
     * @return the change that would result from this include operation
     * @throws ActivateException If unable to include the composite.
     */
    LogicalChange include(LogicalCompositeComponent domain, Composite composite) throws ActivateException;


    /**
     * Exclude the composite from the domain.
     *
     * @param domain    Domain in which the composite is to be included.
     * @param composite Composite to be included in the domain.
     * @return the change that would result from this include operation
     * @throws ActivateException If unable to include the composite.
     */
    LogicalChange exclude(LogicalCompositeComponent domain, Composite composite) throws ActivateException;
}
