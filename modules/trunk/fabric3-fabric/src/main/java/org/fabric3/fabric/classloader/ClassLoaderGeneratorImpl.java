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
import java.util.List;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.model.type.ContributionResourceDescription;
import org.fabric3.spi.model.type.ExtensionResourceDescription;
import org.fabric3.spi.model.type.Implementation;
import org.fabric3.spi.model.type.ResourceDescription;

@EagerInit
public class ClassLoaderGeneratorImpl implements ClassLoaderGenerator {

    public URI generate(LogicalComponent<?> component, GeneratorContext context)
            throws GenerationException {
        PhysicalChangeSet changeSet = context.getPhysicalChangeSet();
        // check to see if the classloader has been created
        LogicalComponent<CompositeImplementation> parent = component.getParent();
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
        Implementation<?> impl = component.getDefinition().getImplementation();
        for (ResourceDescription description : impl.getResourceDescriptions()) {
            if (description instanceof ContributionResourceDescription) {
                ContributionResourceDescription contribDescription = (ContributionResourceDescription) description;
                // add the contribution to the classpath
                definition.addUri(contribDescription.getIdentifier());
            } else if (description instanceof ExtensionResourceDescription) {
                // TODO support
            }
        }
        // add any required resources to the classpath
        processServices(component);
        processReferences(component);
        return definition.getUri();
    }

    private void processServices(LogicalComponent<?> logicalComponent) {
        for (LogicalService service : logicalComponent.getServices()) {
            for (ResourceDescription description : service.getDefinition().getResourceDescriptions()) {
                if (description instanceof ExtensionResourceDescription) {
                    // TODO support
                }
            }
            // check bindings for the service
            processBindings(service.getBindings());
        }
    }

    private void processReferences(LogicalComponent<?> logicalComponent) {
        for (LogicalReference reference : logicalComponent.getReferences()) {
            for (ResourceDescription description : reference.getDefinition().getResourceDescriptions()) {
                if (description instanceof ExtensionResourceDescription) {
                    // TODO support
                }
            }
            // check bindings for the reference
            processBindings(reference.getBindings());
        }
    }

    private void processBindings(List<LogicalBinding> bindings) {
        for (LogicalBinding binding : bindings) {
            for (ResourceDescription description : binding.getBinding().getResourceDescriptions()) {
                if (description instanceof ExtensionResourceDescription) {
                    // TODO support
                }
            }
        }
    }
}
