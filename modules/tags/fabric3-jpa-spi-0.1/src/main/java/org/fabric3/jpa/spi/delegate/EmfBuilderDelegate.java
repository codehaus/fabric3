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
package org.fabric3.jpa.spi.delegate;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

/**
 * Delegate interface for creating entity manager factories for adding 
 * provider specific hook-ins.
 * 
 * @version $Revision$ $Date$
 */
public interface EmfBuilderDelegate {
    
    /**
     * Builds the entity managed factory.
     * 
     * @param info Persistence unit info.
     * @param classLoader Classloader to use.
     * @return Entity manager factory.
     */
    EntityManagerFactory build(PersistenceUnitInfo info, ClassLoader classLoader, String dataSourceName);

}
