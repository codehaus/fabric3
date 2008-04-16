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
package org.fabric3.fabric.classloader;

import java.net.URI;
import java.net.URL;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ResourceDescription;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;
import org.fabric3.spi.model.topology.ClassLoaderResourceDescription;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.model.type.ContributionResourceDescription;
import org.fabric3.spi.services.discovery.DiscoveryService;

/**
 * Default implementation of the ClassLoaderGenerator. This implementation groups components contained within a composite deployed to the same
 * participant in a common classloader. If components are being included in a composite, the existing participant classloader will be updated with
 * required resources and parent classloaders (e.g. when resources are imported from other contributions). If a classloader does not exist on the
 * participant, a new one including parents, will be provisioned.
 */
@EagerInit
public class ClassLoaderGeneratorImpl implements ClassLoaderGenerator {

    private DiscoveryService discoveryService;

    public ClassLoaderGeneratorImpl(@Reference DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public PhysicalClassLoaderDefinition generate(LogicalComponent<?> component) throws GenerationException {

        LogicalComponent<CompositeImplementation> parent = component.getParent();
        Implementation<?> impl = component.getDefinition().getImplementation();

        List<ResourceDescription<?>> descriptions = impl.getResourceDescriptions();

        return generate(parent, descriptions);

    }

    private PhysicalClassLoaderDefinition generate(LogicalComponent<?> parent, List<ResourceDescription<?>> descriptions) throws GenerationException {

        URI runtimeId = parent.getRuntimeId();

        RuntimeInfo info = discoveryService.getRuntimeInfo(runtimeId);

        if (info == null) {
            String id = runtimeId.toString();
            throw new ClassLoaderGenerationException("Runtime not found [" + id + "]", id);
        }

        URI classLoaderUri = parent.getUri();
        PhysicalClassLoaderDefinition definition = new PhysicalClassLoaderDefinition(classLoaderUri);
        LogicalComponent<CompositeImplementation> grandParent = parent.getParent();
        if (grandParent != null) {
            // set the classloader hierarchy if we are not at the domain level
            URI uri = grandParent.getUri();
            definition.addParentClassLoader(uri);
        }
        processPhysicalDefinition(info, definition, descriptions);
        return definition;
    }

    private void processPhysicalDefinition(RuntimeInfo info, PhysicalClassLoaderDefinition definition, List<ResourceDescription<?>> descriptions)
            throws ClassLoaderGenerationException {

        // Determine if a classloader exists on the target participant.
        ClassLoaderResourceDescription clDescription = info.getResourceDescription(ClassLoaderResourceDescription.class, definition.getUri());
        if (clDescription == null) {
            processNew(definition, descriptions);
        } else {
            processUpdate(definition, clDescription, descriptions);
        }
    }

    private void processNew(PhysicalClassLoaderDefinition definition, List<ResourceDescription<?>> descriptions) {

        for (ResourceDescription<?> description : descriptions) {
            if (description instanceof ContributionResourceDescription) {
                ContributionResourceDescription contribDescription = (ContributionResourceDescription) description;
                // add the contribution artifact urls to the classpath
                for (URL url : contribDescription.getArtifactUrls()) {
                    if (url != null && !definition.getResourceUrls().contains(url)) {
                        definition.addResourceUrl(url);
                    }
                }
                for (URI uri : contribDescription.getImportedUris()) {
                    if (uri != null && !definition.getParentClassLoaders().contains(uri)) {
                        definition.addParentClassLoader(uri);
                    }
                }
            }
        }
    }

    private void processUpdate(PhysicalClassLoaderDefinition definition,
                               ClassLoaderResourceDescription clDescription,
                               List<ResourceDescription<?>> descriptions) {

        definition.setUpdate(true);
        for (ResourceDescription<?> description : descriptions) {
            if (description instanceof ContributionResourceDescription) {
                ContributionResourceDescription contribDescription = (ContributionResourceDescription) description;
                // add the contribution artifact urls to the classpath
                for (URL url : contribDescription.getArtifactUrls()) {
                    if (url != null
                            && !clDescription.getClassPathUrls().contains(url)
                            && !definition.getResourceUrls().contains(url)) {
                        definition.addResourceUrl(url);
                    }
                }
                for (URI uri : contribDescription.getImportedUris()) {
                    if (uri != null
                            && !clDescription.getParents().contains(uri)
                            && !definition.getParentClassLoaders().contains(uri)) {
                        definition.addParentClassLoader(uri);
                    }
                }
            }
        }
    }

}
