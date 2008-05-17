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
package org.fabric3.fabric.generator.classloader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;
import org.fabric3.spi.services.contribution.ArtifactLocationEncoder;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Default implementation of the ClassLoaderGenerator. This implementation groups components contained within a composite deployed to the same runtime
 * in a common classloader. If components are being included in a composite, the existing classloader will be updated with required resources and
 * parent classloaders (e.g. when resources are imported from other contributions). If a classloader does not exist on the runtime, a new one,
 * including parents, will be provisioned.
 */
@EagerInit
public class ClassLoaderGeneratorImpl implements ClassLoaderGenerator {
    private MetaDataStore store;
    private ArtifactLocationEncoder encoder;

    @Constructor
    public ClassLoaderGeneratorImpl(@Reference MetaDataStore store) {
        this.store = store;
    }

    /**
     * Setter for injecting the ArtifactLocationEncoder. This is done lazily as the encoder is suppolied by an extension which is intialized after the
     * ClassLoaderGenerator which is needed during bootstrap.
     *
     * @param encoders the encoder to inject
     */
    @Reference(required = false)
    public void setEncoder(List<ArtifactLocationEncoder> encoders) {
        if (encoders == null || encoders.isEmpty()) {
            return;
        }
        // workaround for FABRICTHREE-262: only multiplicity references can be reinjected
        this.encoder = encoders.get(0);
    }

    public PhysicalClassLoaderDefinition generate(LogicalComponent<?> component) throws GenerationException {

        LogicalComponent<CompositeImplementation> parent = component.getParent();
        URI classLoaderUri = parent.getUri();
        PhysicalClassLoaderDefinition definition = new PhysicalClassLoaderDefinition(classLoaderUri);
        LogicalComponent<CompositeImplementation> grandParent = parent.getParent();
        if (grandParent != null) {
            // set the classloader hierarchy if we are not at the domain level
            URI uri = grandParent.getUri();
            definition.addParentClassLoader(uri);
        }
        URI contributionUri = component.getDefinition().getContributionUri();
        if (contributionUri == null) {
            // the logical component is not provisioned as part of a contribution, e.g. a boostrap system service
            return definition;
        }
        Contribution contribution = store.find(contributionUri);
        URI runtimeId = component.getRuntimeId();
        updateUrl(runtimeId, definition, contributionUri);
        for (URI uri : contribution.getResolvedImportUris()) {
            if (!definition.getParentClassLoaders().contains(uri)) {
                definition.addParentClassLoader(uri);
            }
        }
        return definition;
    }

    private void updateUrl(URI runtimeId, PhysicalClassLoaderDefinition definition, URI contributionUri) throws ClassLoaderGenerationException {
        if (runtimeId != null) {
            // if the target runtime is not the current one, encode the contribution URI so it can be dereferenced from another VM
            try {
                contributionUri = encoder.encode(contributionUri);
            } catch (URISyntaxException e) {
                throw new ClassLoaderGenerationException(e);
            }
        }
        if (!definition.getContributionUris().contains(contributionUri)) {
            definition.addContributionUri(contributionUri);
        }
    }

}
