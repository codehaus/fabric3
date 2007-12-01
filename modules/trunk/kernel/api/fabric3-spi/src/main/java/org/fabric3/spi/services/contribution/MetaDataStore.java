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
package org.fabric3.spi.services.contribution;

import java.net.URI;

/**
 * Implementations store contribution metadata
 *
 * @version $Rev$ $Date$
 */
public interface MetaDataStore {

    /**
     * Returns the store id.
     *
     * @return the store id
     */
    String getId();

    /**
     * Stores the contribution metadata
     *
     * @param contribution the contribution metadata
     * @throws MetaDataStoreException if an error storing the metadata occurs
     */
    void store(Contribution contribution) throws MetaDataStoreException;

    /**
     * Returns the contribution for the given URI
     *
     * @param contributionUri the contribution URI
     * @return the contribution for the given URI or null if not found
     */
    Contribution find(URI contributionUri);

    /**
     * Resolves a resource element by its symbol against the entire domain symbol space.
     *
     * @param symbol the symbol used to represent the resource element.
     * @return the resource element
     */
    <S extends Symbol> ResourceElement<S, ?> resolve(S symbol);

    /**
     * Resolves a resource element by its symbol against the given contribution.
     *
     * @param contribution the contribution to resolve against
     * @param type         the class representing the resource
     * @param symbol       the symbol used to represent the resource element.
     * @return the resource element
     */
    <S extends Symbol, V> ResourceElement<S, V> resolve(Contribution contribution, Class<V> type, S symbol);

    /**
     * Resolves an import to a matching export
     *
     * @param imprt the import to resolve
     * @return a matching contribution or null
     */
    Contribution resolve(Import imprt);

}
