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
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.model.topology.ClassLoaderResourceDescription;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ResourceDescription;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.model.type.ContributionResourceDescription;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.spi.util.UriHelper;

/**
 * Default implementation of the ClassLoaderGenerator. This implementation groups components contained within a
 * composite deployed to the same participant in a common classloader. If components are being included in a composite,
 * the existing participant classloader will be updated with required resources and parent classloaders (e.g. when
 * resources are imported from other contributions). If a classloader does not exist on the participant, a new one
 * including parents, will be provisioned.
 */
@EagerInit
public class ClassLoaderGeneratorImpl implements ClassLoaderGenerator {
    private DiscoveryService discoveryService;

    public ClassLoaderGeneratorImpl(@Reference DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    public URI generate(LogicalComponent<?> component, GeneratorContext context) throws GenerationException {
        LogicalComponent<CompositeImplementation> parent = component.getParent();
        Implementation<?> impl = component.getDefinition().getImplementation();
        List<ResourceDescription> descriptions = impl.getResourceDescriptions();
        URI runtimeId = component.getRuntimeId();
        return generate(parent, runtimeId, descriptions, context);
    }

    public URI generate(LogicalResource<?> resource, GeneratorContext context) throws GenerationException {
        LogicalComponent<?> component = resource.getParent();
        LogicalComponent<CompositeImplementation> parent = component.getParent();
        List<ResourceDescription> descriptions = new ArrayList<ResourceDescription>();
        descriptions.addAll(resource.getResourceDefinition().getResourceDescriptions());
        URI runtimeId = component.getRuntimeId();
        return generate(parent, runtimeId, descriptions, context);
    }

    @SuppressWarnings({"unchecked"})
    public URI generate(LogicalBinding<?> binding, GeneratorContext context) throws GenerationException {
        Bindable bindable = binding.getParent();
        LogicalComponent<?> parent = bindable.getParent();
        List<ResourceDescription> descriptions = new ArrayList<ResourceDescription>();
        if (bindable instanceof LogicalReference) {
            ReferenceDefinition definition = ((LogicalReference) bindable).getDefinition();
            for (ResourceDescription description : definition.getResourceDescriptions()) {
                if (!descriptions.contains(description)) {
                    descriptions.add(description);
                }
            }
        } else if (bindable instanceof LogicalService) {
            ServiceDefinition definition = ((LogicalService) bindable).getDefinition();
            for (ResourceDescription description : definition.getResourceDescriptions()) {
                if (!descriptions.contains(description)) {
                    descriptions.add(description);
                }
            }
        }
        for (ResourceDescription description : binding.getBinding().getResourceDescriptions()) {
            if (!descriptions.contains(description)) {
                descriptions.add(description);
            }
        }
        URI runtimeUri = parent.getRuntimeId();
        return generate(parent, runtimeUri, descriptions, context);
    }

    private URI generate(LogicalComponent<?> parent,
                         URI runtimeUri,
                         List<ResourceDescription> descriptions,
                         GeneratorContext context) throws GenerationException {
        String runtimeId = null;
        // TODO fix this by making runtime ids URI's
        if (runtimeUri != null) {
            runtimeId = runtimeUri.toString();
        }
        RuntimeInfo info = discoveryService.getRuntimeInfo(runtimeId);
        if (info == null) {
            throw new ClassLoaderGenerationException("Runtime not found [" + runtimeId + "]", runtimeId);
        }

        PhysicalChangeSet changeSet = context.getPhysicalChangeSet();
        // check to see if the classloader definition has been created as part of the current changeset
        URI classLoaderUri = parent.getUri();

        PhysicalClassLoaderDefinition definition =
                changeSet.getResourceDefinition(PhysicalClassLoaderDefinition.class, classLoaderUri);
        if (definition == null) {
            // classloader definition has not been created during generation, create one including parents if necessary.
            definition = new PhysicalClassLoaderDefinition(classLoaderUri);
            LogicalComponent<CompositeImplementation> grandParent = parent.getParent();
            if (grandParent != null) {
                // set the classloader hierarchy if we are not at the domain level
                URI uri = grandParent.getUri();
                definition.addParentClassLoader(uri);
                if (discoveryService != null) {
                    generateClassLoaderHierarchy(uri, info, context);
                }
            }
            changeSet.addResourceDefinition(definition);
            processPhysicalDefinition(info, definition, descriptions);
        } else {
            // process the existing definition
            processPhysicalDefinition(info, definition, descriptions);
        }
        return definition.getUri();
    }

    private void processPhysicalDefinition(RuntimeInfo info,
                                           PhysicalClassLoaderDefinition definition,
                                           List<ResourceDescription> descriptions)
            throws ClassLoaderGenerationException {
        // Determine if a classloader exists on the target participant.
        ClassLoaderResourceDescription clDescription =
                info.getResourceDescription(ClassLoaderResourceDescription.class, definition.getUri());
        if (clDescription == null) {
            processNew(definition, descriptions);
        } else {
            processUpdate(definition, clDescription, descriptions);
        }
    }

    private void processNew(PhysicalClassLoaderDefinition definition, List<ResourceDescription> descriptions) {
        for (ResourceDescription description : descriptions) {
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
                               List<ResourceDescription> descriptions) {
        definition.setUpdate(true);
        for (ResourceDescription description : descriptions) {
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

    /**
     * Generates parent classloaders if they are not present for a given uri and adds them to the current generator
     * context.
     *
     * @param uri     the uri
     * @param info    the current runtime info
     * @param context the generator context
     */
    private void generateClassLoaderHierarchy(URI uri, RuntimeInfo info, GeneratorContext context) {
        ClassLoaderResourceDescription desc = info.getResourceDescription(ClassLoaderResourceDescription.class, uri);
        PhysicalChangeSet changeSet = context.getPhysicalChangeSet();
        if (desc != null) {
            return;
        } else {
            PhysicalClassLoaderDefinition definition =
                    changeSet.getResourceDefinition(PhysicalClassLoaderDefinition.class, uri);
            if (definition != null) {
                return;
            }
        }

        URI parentUri = URI.create(UriHelper.getParentName(uri));
        if (parentUri != null) {
            generateClassLoaderHierarchy(parentUri, info, context);
        }

        PhysicalClassLoaderDefinition definition = new PhysicalClassLoaderDefinition(uri);
        // set the classloader hierarchy if we are not at the domain level
        definition.addParentClassLoader(parentUri);
        changeSet.addResourceDefinition(definition);
    }


}
