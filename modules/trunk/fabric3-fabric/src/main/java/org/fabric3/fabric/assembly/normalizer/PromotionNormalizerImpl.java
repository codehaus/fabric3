package org.fabric3.fabric.assembly.normalizer;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.model.type.SCABindingDefinition;
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
                return;
            }
            service.overrideBindings(bindings);
        }
    }

    private List<LogicalBinding<?>> recurseServicePromotionPath(LogicalComponent<CompositeImplementation> parent,
                                                             URI serviceUri) {
        List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
        for (LogicalService service : parent.getServices()) {
            URI targetUri = service.getPromote();
            if (targetUri.getFragment() == null) {
                // no service specified
                if (targetUri.equals(UriHelper.getDefragmentedName(serviceUri))) {
                    if (parent.getParent() != null) {
                        List<LogicalBinding<?>> list = recurseServicePromotionPath(parent.getParent(), service.getUri());
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
                        List<LogicalBinding<?>> list = recurseServicePromotionPath(parent.getParent(), service.getUri());
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
                targets.addAll(promoted.getTargetUris());
            }
            if (!bindings.isEmpty()) {
                reference.overrideBindings(bindings);
            } else if (reference.getBindings().size() == 0) {
                // no bindings were configured so use the SCA Binding
                SCABindingDefinition definition = SCABindingDefinition.INSTANCE;
                LogicalBinding<SCABindingDefinition> binding = new LogicalBinding<SCABindingDefinition>(definition, reference);
                reference.addBinding(binding);
            }
            if (!targets.isEmpty()) {
                reference.overrideTargets(targets);
            }
        }
    }

    private List<LogicalReference> recurseReferencePromotionPath(LogicalComponent<CompositeImplementation> parent,
                                                                 URI referenceUri) {
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
