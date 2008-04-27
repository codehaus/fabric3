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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;
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

    public ClassLoaderGeneratorImpl(@Reference MetaDataStore store) {
        this.store = store;
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
        for (URL url : contribution.getArtifactUrls()) {
            if (!definition.getResourceUrls().contains(url)) {
                definition.addResourceUrl(url);
            }
        }
        for (URI uri : contribution.getResolvedImportUris()) {
            if (!definition.getParentClassLoaders().contains(uri)) {
                definition.addParentClassLoader(uri);
            }
        }
        return definition;
    }

}
