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
package org.fabric3.spi.services.contribution;

import org.fabric3.spi.services.archive.ArchiveStore;

/**
 * A registry of persistent stores used by a contribution service
 *
 * @version $Rev$ $Date$
 */
public interface ContributionStoreRegistry {

    /**
     * Register an ArchiveStore.
     *
     * @param store the ArchiveStore
     */
    void register(ArchiveStore store);

    /**
     * Unegister an ArchiveStore.
     *
     * @param store the ArchiveStore
     */
    void unregister(ArchiveStore store);

    /**
     * Register an MetaDataStore.
     *
     * @param store the MetaDataStore
     */
    void register(MetaDataStore store);

    /**
     * Unregister an MetaDataStore.
     *
     * @param store the MetaDataStore
     */
    void unregister(MetaDataStore store);

}
