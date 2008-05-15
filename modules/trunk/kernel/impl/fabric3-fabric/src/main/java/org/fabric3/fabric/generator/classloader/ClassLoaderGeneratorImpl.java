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
import java.net.URL;
import java.net.MalformedURLException;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Constructor;

import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.ArtifactLocationEncoder;

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

    public ClassLoaderGeneratorImpl(MetaDataStore store) {
        this.store = store;
    }

    @Constructor
    public ClassLoaderGeneratorImpl(@Reference MetaDataStore store, @Reference ArtifactLocationEncoder encoder) {
        this.store = store;
        this.encoder = encoder;
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
        URL location = contribution.getLocation();
        URI runtimeId = component.getRuntimeId();
        updateUrl(runtimeId, definition, location);
        for (URL url : contribution.getDependencyUrls()) {
            updateUrl(runtimeId, definition, url);
        }
        for (URI uri : contribution.getResolvedImportUris()) {
            if (!definition.getParentClassLoaders().contains(uri)) {
                definition.addParentClassLoader(uri);
            }
        }
        return definition;
    }

    private void updateUrl(URI runtimeId, PhysicalClassLoaderDefinition definition, URL location) throws ClassLoaderGenerationException {
        if (runtimeId != null) {
            // if the target runtime is not this one, encode the artifact URL so it can be dereferenced from another VM
            try {
                location = encoder.encode(location);
            } catch (MalformedURLException e) {
                throw new ClassLoaderGenerationException(e);
            }
        }
        if (!definition.getResourceUrls().contains(location)) {
            definition.addResourceUrl(location);
        }
    }

}
