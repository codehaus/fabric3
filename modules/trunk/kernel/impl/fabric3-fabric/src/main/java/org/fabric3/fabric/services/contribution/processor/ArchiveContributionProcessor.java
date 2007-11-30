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
package org.fabric3.fabric.services.contribution.processor;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.contribution.ContributionProcessorExtension;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.model.type.ContributionResourceDescription;
import org.fabric3.spi.services.contribution.ArtifactLocationEncoder;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;

/**
 * Handles common processing for contribution archives
 *
 * @version $Rev$ $Date$
 */
public abstract class ArchiveContributionProcessor extends ContributionProcessorExtension {
    protected ArtifactLocationEncoder encoder;
    private MetaDataStore store;

    protected ArchiveContributionProcessor(@Reference MetaDataStore store,
                                           @Reference ArtifactLocationEncoder encoder) {
        this.store = store;
        this.encoder = encoder;
    }

    public void processContent(Contribution contribution, ClassLoader loader) throws ContributionException {
        // process the contribution manifest
        // Build a classloader to perform the contribution introspection. The classpath will contain the contribution
        // jar and resolved imports
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            iterateArtifacts(contribution, new Action() {
                public void process(Contribution contribution, String contentType, InputStream stream)
                        throws ContributionException {
                    Resource resource = registry.processResource(contentType, stream);
                    if (resource != null) {
                        contribution.addResource(resource);
                    }
                }
            });
            addContributionDescription(contribution);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }
    }

    protected abstract void iterateArtifacts(Contribution contribution, Action action)
            throws ContributionException;

    /**
     * Recursively adds a resource description pointing to the contribution artifact on contained components.
     *
     * @param contribution the contribution the resource description requires
     * @throws ContributionNotFoundException if a required imported contribution is not found
     */
    private void addContributionDescription(Contribution contribution) throws ContributionNotFoundException {
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

        for (Resource resource : contribution.getResources()) {
            // XCV FIXME specific composite case
            for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                Object value = element.getValue();
                if (value instanceof Composite) {
                    addContributionDescription(description, (Composite) value);
                }
            }
        }
    }

    /**
     * Adds the given resource description pointing to the contribution artifact on contained components.
     *
     * @param description the resource description
     * @param composite   the component type to introspect
     */
    private void addContributionDescription(ContributionResourceDescription description, Composite composite) {
        for (ComponentDefinition<?> definition : composite.getComponents().values()) {
            Implementation<?> implementation = definition.getImplementation();
            if (CompositeImplementation.class.isInstance(implementation)) {
                CompositeImplementation compositeImplementation = CompositeImplementation.class.cast(implementation);
                Composite componentType = compositeImplementation.getComponentType();
                addContributionDescription(description, componentType);
            } else {
                implementation.addResourceDescription(description);
                // mark references and services as well;
                AbstractComponentType<?, ?, ?, ?> type = implementation.getComponentType();
                for (ServiceDefinition service : type.getServices().values()) {
                    service.addResourceDescription(description);
                }
                for (ReferenceDefinition reference : type.getReferences().values()) {
                    reference.addResourceDescription(description);
                }
                for (ResourceDefinition resource : type.getResources().values()) {
                    resource.addResourceDescription(description);
                }
            }
        }
    }

}
