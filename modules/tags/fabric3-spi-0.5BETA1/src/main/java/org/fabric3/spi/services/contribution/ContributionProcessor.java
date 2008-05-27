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

import java.util.List;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.scdl.ValidationContext;

/**
 * Interface for services that process contributions. Contribution processing occurs in several phases. Contribution metadata is first processed,
 * after which contained resources are indexed. Indexed {@link Resource}s contain 0..n {@link ResourceElement}s, which are addressable parts.
 * ResourceElements contain a key for a symbol space and a value. When a resource is indexed, only ResourceElement keys are available; their values
 * have not yet been loaded.
 * <p/>
 * The final processing phase is when the contribution is loaded. At this point, all contribution artifacts, including those in depedent
 * contributions, are made available through the provided classloader. Indexed Resources are iterated and all ResourceElement values are loaded via
 * the loader framework. As ResourceElements may refer to other ResourceElements, loading may ocurr recursively.
 *
 * @version $Rev$ $Date$
 */
public interface ContributionProcessor {
    /**
     * Returns the content type this implementation handles.
     *
     * @return the content type this implementation handles
     */
    public abstract List<String> getContentTypes();

    /**
     * Processses manifest information for the contribution, including imports and exports.
     *
     * @param contribution the contribution that will be used to hold the results from the processing
     * @param context      the context to which validation errors and warnings are reported
     * @throws ContributionException if there was a problem with the contribution
     */
    void processManifest(Contribution contribution, ValidationContext context) throws ContributionException;

    /**
     * Indexes all contribution resources
     *
     * @param contribution the contribution to index
     * @param context      the context to which validation errors and warnings are reported
     * @throws ContributionException if there was a problem indexing
     */
    void index(Contribution contribution, ValidationContext context) throws ContributionException;

    /**
     * Loads all resources in the contribution.
     *
     * @param contribution the contribution
     * @param context      the context to which validation errors and warnings are reported
     * @param loader       the classloader contribution resources must be loaded in
     * @throws ContributionException if there was a problem loading the contribution resoruces
     */
    public void process(Contribution contribution, ValidationContext context, ClassLoader loader) throws ContributionException;

}
