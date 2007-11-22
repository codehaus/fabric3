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
package org.fabric3.spi.domain;

import java.net.URI;
import java.util.Collection;

import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.spi.assembly.RecordException;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * @version $Revision$ $Date$
 */
public interface DomainService {
    
    /**
     * Gets a reference to the domain.
     * 
     * @return Reference to the current domain.
     */
    LogicalComponent<CompositeImplementation> getDomain();
    
    /**
     * Finds a logical component by URI.
     * 
     * @param uri URI of the component.
     * @return Logical component.
     */
    LogicalComponent<?> findComponent(URI uri);
    
    /**
     * Initializes the domain service.
     */
    void initialize() throws AssemblyException;
    
    /**
     * Gets all logical components in the domain.
     * 
     * @return Logical components in the domain.
     */
    Collection<LogicalComponent<?>> getComponents();
    
    /**
     * Stores the domain using assembly store.
     * 
     * @throws RecordException If unable to store the domain.
     */
    void store() throws RecordException;

}
