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
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Implementations instantiate logical components within a domain.
 *
 * @version $Revision$ $Date$
 */
public interface LogicalModelInstantiator {

    /**
     * Creates a LogicalChange for including a composite in another composite.
     *
     * @param targetComposite the target composite in which the composite is to be included.
     * @param composite       the composite to be included.
     * @return the change that would result from this include operation
     * @throws LogicalInstantiationException If unable to include the composite.
     */
    LogicalChange include(LogicalCompositeComponent targetComposite, Composite composite) throws LogicalInstantiationException;


    /**
     * Creates a LogicalChange for removing the composite from the target composite.
     *
     * @param targetComposite the target composite from which the composite is to be removed.
     * @param composite       Composite to be removed.
     * @return the change that would result from this remove operation
     * @throws LogicalInstantiationException If unable to remove the composite.
     */
    LogicalChange remove(LogicalCompositeComponent targetComposite, Composite composite) throws LogicalInstantiationException;
}
