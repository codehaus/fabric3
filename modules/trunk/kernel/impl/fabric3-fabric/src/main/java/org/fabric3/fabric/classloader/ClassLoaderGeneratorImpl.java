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
import org.fabric3.spi.model.type.ContributionResourceDescription;
import org.fabric3.spi.model.type.ExtensionResourceDescription;

@EagerInit
public class ClassLoaderGeneratorImpl implements ClassLoaderGenerator {

    public URI generate(LogicalComponent<?> component, GeneratorContext context) throws GenerationException {
        LogicalComponent<CompositeImplementation> parent = component.getParent();
        Implementation<?> impl = component.getDefinition().getImplementation();
        List<ResourceDescription> descriptions = impl.getResourceDescriptions();
        return generate(parent, descriptions, context);
    }
    
    public URI generate(LogicalResource<?> resource, GeneratorContext context) throws GenerationException {
        
        LogicalComponent<?> component = resource.getParent();
        LogicalComponent<CompositeImplementation> parent =
            (LogicalComponent<CompositeImplementation>) component.getParent();

        List<ResourceDescription> descriptions = new ArrayList<ResourceDescription>();
        descriptions.addAll(resource.getResourceDefinition().getResourceDescriptions());
        
        return generate(parent, descriptions, context);
        
    }

    public URI generate(LogicalBinding<?> binding, GeneratorContext context) throws GenerationException {
        Bindable bindable = binding.getParent();
        LogicalComponent<CompositeImplementation> parent =
                (LogicalComponent<CompositeImplementation>) bindable.getParent();
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
        return generate(parent, descriptions, context);
    }

    private URI generate(LogicalComponent<CompositeImplementation> parent,
                         List<ResourceDescription> resourceDescriptions,
                         GeneratorContext context) throws GenerationException {
        PhysicalChangeSet changeSet = context.getPhysicalChangeSet();
        // check to see if the classloader has been created
        URI parentUri = parent.getUri();
        // first check to see if a classloader definition has already been created. If not, create one.
        PhysicalClassLoaderDefinition definition =
                changeSet.getResourceDefinition(PhysicalClassLoaderDefinition.class, parentUri);
        if (definition == null) {
            definition = new PhysicalClassLoaderDefinition(parentUri);
            LogicalComponent<CompositeImplementation> grandParent = parent.getParent();
            if (grandParent != null) {
                // set the classloader hierarchy if we are not at the domain level
                definition.addParentClassLoader(grandParent.getUri());
            }
            changeSet.addResourceDefinition(definition);
        }

        for (ResourceDescription description : resourceDescriptions) {
            if (description instanceof ContributionResourceDescription) {
                ContributionResourceDescription contribDescription = (ContributionResourceDescription) description;
                // add the contribution and imported urls to the classpath
                for (URL url : contribDescription.getArtifactUrls()) {
                    if (url != null && !definition.getResourceUrls().contains(url)) {
                        definition.addResourceUrl(url);
                    }
                }
            } else if (description instanceof ExtensionResourceDescription) {
                // TODO support
            }
        }
        return definition.getUri();
    }

}
