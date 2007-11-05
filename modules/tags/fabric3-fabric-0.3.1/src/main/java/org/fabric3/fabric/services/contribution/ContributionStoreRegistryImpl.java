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
package org.fabric3.fabric.services.contribution;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.services.archive.ArchiveStore;
import org.fabric3.spi.services.contribution.ContributionStoreRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Default implementation of the ContributionStoreRegistry
 *
 * @version $Rev$ $Date$
 */
public class ContributionStoreRegistryImpl implements ContributionStoreRegistry {
    private Map<String, ArchiveStore> archiveStores;
    private Map<String, MetaDataStore> metaDataStores;

    public ContributionStoreRegistryImpl() {
        archiveStores = new HashMap<String, ArchiveStore>();
        metaDataStores = new HashMap<String, MetaDataStore>();
    }

    public void register(ArchiveStore store) {
        archiveStores.put(store.getId(), store);
    }

    public void unregister(ArchiveStore store) {
        metaDataStores.remove(store.getId());
    }

    public ArchiveStore getArchiveStore(String storeId) {
        return archiveStores.get(storeId);
    }

    public void register(MetaDataStore store) {
        metaDataStores.put(store.getId(), store);
    }

    public void unregister(MetaDataStore store) {
        metaDataStores.remove(store.getId());
    }

    public MetaDataStore getMetadataStore(String storeId) {
        return metaDataStores.get(storeId);
    }


}
