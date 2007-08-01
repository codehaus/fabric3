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
package org.fabric3.extension.contribution;

import java.net.URI;
import java.net.URL;

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ModelObject;
import org.fabric3.spi.model.type.ContributionResourceDescription;
import org.fabric3.spi.services.contribution.ArtifactLocationEncoder;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.StoreNotFoundException;

/**
 * Handles common processing for archives
 *
 * @version $Rev$ $Date$
 */
public abstract class ArchiveContributionProcessor extends ContributionProcessorExtension {
    protected MetaDataStore store;
    protected ArtifactLocationEncoder encoder;

    protected ArchiveContributionProcessor(@Reference MetaDataStore store, @Reference ArtifactLocationEncoder encoder) {
        this.store = store;
        this.encoder = encoder;
    }

    /**
     * Recursively adds a resource description pointing to the contribution artifact on contained components.
     *
     * @param contribution the contribution the resource description requires
     * @throws StoreNotFoundException        if no store can be found for a contribution import
     * @throws ContributionNotFoundException if a required imported contribution is not found
     */
    protected void addContributionDescription(Contribution contribution)
            throws StoreNotFoundException, ContributionNotFoundException {
        ContributionResourceDescription description = new ContributionResourceDescription(contribution.getUri());
        // encode the contribution URL so it can be dereferenced remotely
        URL encodedLocation = encoder.encode(contribution.getLocation());
        description.addArtifactUrl(encodedLocation);
        // Obtain local URLs for imported contributions and encode them for remote dereferencing
        for (URI uri : contribution.getResolvedImportUris()) {
            Contribution imported = store.find(uri);
            if (imported == null) {
                throw new ContributionNotFoundException("Imported contribution not found", uri.toString());
            }
            URL importedUrl = encoder.encode(imported.getLocation());
            description.addArtifactUrl(importedUrl);
        }
        for (ModelObject type : contribution.getTypes().values()) {
            if (type instanceof Composite) {
                addContributionDescription(description, (Composite) type);
            }
        }
    }

    /**
     * Adds the given resource description pointing to the contribution artifact on contained components.
     *
     * @param description the resource description
     * @param type        the component type to introspect
     */
    protected void addContributionDescription(ContributionResourceDescription description, Composite type) {
        for (ComponentDefinition<?> definition : type.getComponents().values()) {
            Implementation<?> implementation = definition.getImplementation();
            if (CompositeImplementation.class.isInstance(implementation)) {
                CompositeImplementation compositeImplementation = CompositeImplementation.class.cast(implementation);
                Composite componentType = compositeImplementation.getComponentType();
                addContributionDescription(description, componentType);
            } else {
                implementation.addResourceDescription(description);
            }
        }
    }

}
