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

package org.fabric3.fabric.services.contribution;

import static org.fabric3.spi.services.contribution.ContributionConstants.DEFAULT_STORE;

import java.io.IOException;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.services.archive.ArchiveStoreImpl;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.contribution.ContributionStoreRegistry;
import org.fabric3.spi.services.contribution.ContributionConstants;

/**
 * The default implementation of ContributionStore
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ContributionStoreImpl extends ArchiveStoreImpl {
    private final ContributionStoreRegistry registry;

    /**
     * Creates a new repository service instance
     *
     * @param repository the repository location
     * @param hostInfo   the host info for the runtime
     * @param registry   the contribution store registry this store will register with
     * @throws java.io.IOException if an error occurs initializing the repository
     */
    public ContributionStoreImpl(@Property(name = "repository")String repository,
                                 @Reference HostInfo hostInfo,
                                 @Reference ContributionStoreRegistry registry) throws IOException {
        super(repository, hostInfo);
        this.registry = registry;
        storeId = DEFAULT_STORE;
    }

    @Constructor
    @Deprecated
    // JFM FIXME remove when properties work
    public ContributionStoreImpl(@Reference HostInfo hostInfo, @Reference ContributionStoreRegistry registry)
            throws IOException {
        this(null, hostInfo, registry);
    }


    @Init
    public void init() throws IOException {
        super.init();
        registry.register(this);
    }
}
