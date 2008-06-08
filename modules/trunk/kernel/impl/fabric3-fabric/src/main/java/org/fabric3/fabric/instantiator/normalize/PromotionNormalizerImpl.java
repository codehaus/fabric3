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
package org.fabric3.fabric.instantiator.normalize;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.util.UriHelper;

/**
 * Default implementation of the BindingNormalizer
 *
 * @version $Rev$ $Date$
 */
public class PromotionNormalizerImpl implements PromotionNormalizer {

    public void normalize(LogicalComponent<?> component) {
        normalizeServiceBindings(component);
        normalizeReferenceBindings(component);
    }

    private void normalizeServiceBindings(LogicalComponent<?> component) {
        LogicalComponent<CompositeImplementation> parent = component.getParent();
        for (LogicalService service : component.getServices()) {
            URI serviceUri = service.getUri();
            List<LogicalBinding<?>> bindings = recurseServicePromotionPath(parent, serviceUri);
            if (bindings.isEmpty()) {
                continue;
            }
            service.overrideBindings(resetParent(bindings, service));
        }
    }

    @SuppressWarnings({"unchecked"})
    private List<LogicalBinding<?>> resetParent(List<LogicalBinding<?>> list, Bindable parent) {
        List<LogicalBinding<?>> newList = new ArrayList<LogicalBinding<?>>();
        for (LogicalBinding<?> binding : list) {
            newList.add(new LogicalBinding(binding.getBinding(), parent));
        }
        return newList;
    }

    private List<LogicalBinding<?>> recurseServicePromotionPath(LogicalComponent<CompositeImplementation> parent, URI serviceUri) {
        List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
        for (LogicalService service : parent.getServices()) {
            URI targetUri = service.getPromotedUri();
            if (targetUri.getFragment() == null) {
                // no service specified
                if (targetUri.equals(UriHelper.getDefragmentedName(serviceUri))) {
                    if (parent.getParent() != null) {
                        List<LogicalBinding<?>> list =
                                recurseServicePromotionPath(parent.getParent(), service.getUri());
                        if (list.isEmpty()) {
                            // no bindings were overridden
                            bindings.addAll(service.getBindings());
                        } else {
                            bindings.addAll(list);
                        }
                    } else {
                        bindings.addAll(service.getBindings());
                    }
                }

            } else {
                if (targetUri.equals(serviceUri)) {
                    if (parent.getParent() != null) {
                        List<LogicalBinding<?>> list =
                                recurseServicePromotionPath(parent.getParent(), service.getUri());
                        if (list.isEmpty()) {
                            // no bindings were overridden
                            bindings.addAll(service.getBindings());
                        } else {
                            bindings.addAll(list);
                        }

                    } else {
                        bindings.addAll(service.getBindings());
                    }
                }
            }
        }
        return bindings;
    }

    private void normalizeReferenceBindings(LogicalComponent<?> component) {
        LogicalComponent<CompositeImplementation> parent = component.getParent();
        for (LogicalReference reference : component.getReferences()) {
            URI referenceUri = reference.getUri();
            List<LogicalReference> references = recurseReferencePromotionPath(parent, referenceUri);
            if (references.isEmpty()) {
                continue;
            }
            List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
            List<URI> targets = new ArrayList<URI>();
            for (LogicalReference promoted : references) {
                bindings.addAll(promoted.getBindings());
                for (LogicalWire logicalWire : promoted.getWires()) {
                    targets.add(logicalWire.getTargetUri());
                }
            }
            if (!bindings.isEmpty()) {
                reference.overrideBindings(bindings);
            }
            if (!targets.isEmpty()) {
                reference.overrideTargets(targets);
            }
        }
    }

    private List<LogicalReference> recurseReferencePromotionPath(LogicalComponent<CompositeImplementation> parent, URI referenceUri) {
        List<LogicalReference> references = new ArrayList<LogicalReference>();
        for (LogicalReference reference : parent.getReferences()) {
            for (URI targetUri : reference.getPromotedUris()) {
                if (targetUri.equals(referenceUri)) {
                    if (parent.getParent() != null) {
                        List<LogicalReference> list =
                                recurseReferencePromotionPath(parent.getParent(), reference.getUri());
                        if (list.isEmpty()) {
                            // no references were overridden
                            references.add(reference);
                        } else {
                            references.addAll(list);
                        }

                    } else {
                        references.add(reference);
                    }
                }
            }
        }
        return references;
    }
}
