package org.fabric3.fabric.wire.resolve;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Autowire;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.TargetResolutionException;
import org.fabric3.spi.wire.TargetResolutionService;

/**
 * Resolution based on an explicitly requested target.
 * 
 * @version $Revsion$ $Date$
 * 
 */
public class TypeBasedAutoWireService implements TargetResolutionService {

    public void resolve(LogicalReference logicalReference, LogicalCompositeComponent context) throws TargetResolutionException {

        ComponentReference componentReference = logicalReference.getComponentReference();
        LogicalComponent<?> component = logicalReference.getParent();
        ReferenceDefinition referenceDefinition = logicalReference.getDefinition();
        String referenceName = referenceDefinition.getName();

        if (componentReference == null) {
            if (!logicalReference.getBindings().isEmpty() || isPromoted(context, component, referenceName)) {
                return;
            }
            ServiceContract<?> requiredContract = determineContract(logicalReference);

            Autowire autowire = calculateAutowire(context, component);
            if (autowire == Autowire.ON) {
                resolveByType(context, component, referenceName, requiredContract);
            }

        } else {

            List<URI> uris = componentReference.getTargets();
            if (!uris.isEmpty() || isPromoted(context, component, referenceName)) {
                return;
            }

            if (componentReference.isAutowire()) {
                ServiceContract<?> requiredContract = referenceDefinition.getServiceContract();
                String fragment = componentReference.getName();
                List<URI> targetUris = resolveByType(component.getParent(), component, referenceName, requiredContract);
                if (targetUris.isEmpty()) {
                    targetUris = resolveByType(context, component, fragment, requiredContract);
                }
            }
        }
        
        if (logicalReference.getWires().isEmpty() && logicalReference.getDefinition().isRequired() && logicalReference.getBindings().isEmpty()) {
            throw new TargetResolutionException("Unable to resolve reference " + logicalReference.getUri());
        }

    }

    private Autowire calculateAutowire(LogicalComponent<?> composite, LogicalComponent<?> component) {

        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();

        // check for an overridden value
        Autowire overrideAutowire = component.getAutowireOverride();
        if (overrideAutowire == Autowire.OFF || overrideAutowire == Autowire.ON) {
            return overrideAutowire;
        }

        Autowire autowire = definition.getAutowire();
        if (autowire == Autowire.INHERITED) {
            // check in the parent composite definition
            if (component.getParent() != null) {
                ComponentDefinition<? extends Implementation<?>> def = component.getParent().getDefinition();
                AbstractComponentType<?, ?, ?, ?> type = def.getImplementation().getComponentType();
                autowire = (Composite.class.cast(type)).getAutowire();
                if (autowire == Autowire.OFF || autowire == Autowire.ON) {
                    return autowire;
                }
            }
            // undefined in the original parent or the component is top-level,
            // check in the target
            ComponentDefinition<? extends Implementation<?>> parentDefinition = composite.getDefinition();
            AbstractComponentType<?, ?, ?, ?> parentType = parentDefinition.getImplementation().getComponentType();
            while (Composite.class.isInstance(parentType)) {
                autowire = (Composite.class.cast(parentType)).getAutowire();
                if (autowire == Autowire.OFF || autowire == Autowire.ON) {
                    break;
                }
                composite = composite.getParent();
                if (composite == null) {
                    break;
                }
                parentDefinition = composite.getDefinition();
                parentType = parentDefinition.getImplementation().getComponentType();
            }
        }

        return autowire;

    }

    private List<URI> resolveByType(LogicalCompositeComponent composite, LogicalComponent<?> component, String name, ServiceContract<?> contract) {
        
        List<URI> candidates = new ArrayList<URI>();
        
        for (LogicalComponent<?> child : composite.getComponents()) {
            ComponentDefinition<? extends Implementation<?>> candidate = child.getDefinition();
            Implementation<?> candidateImpl = candidate.getImplementation();
            AbstractComponentType<?, ?, ?, ?> candidateType = candidateImpl.getComponentType();
            for (ServiceDefinition service : candidateType.getServices().values()) {
                ServiceContract<?> targetContract = service.getServiceContract();
                if (targetContract == null) {
                    continue;
                }
                if (contract.isAssignableFrom(targetContract)) {
                    candidates.add(URI.create(child.getUri().toString() + '#' + service.getName()));
                    break;
                }
            }
        }
        
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        for (URI target : candidates) {
            LogicalReference logicalReference = component.getReference(name);
            assert logicalReference != null;
            logicalReference.addTargetUri(component.getUri().resolve(target));
        }

        return candidates;
        
    }
    
    private boolean isPromoted(LogicalComponent<?> composite, LogicalComponent<?> component, String referenceName) {
        
        for (LogicalReference compositeReference : composite.getReferences()) {
            List<URI> uris = compositeReference.getPromotedUris();
            if (component.getReferences().size() == 1) {
                LogicalReference componentRef = component.getReferences().iterator().next();
                for (URI uri : uris) {
                    if (uri.getFragment() == null && component.getUri().equals(uri)) {
                        return true;
                    } else {
                        if (componentRef.getUri().equals(uri)) {
                            return true;
                        }
                    }
                }
            } else {
                URI refUri = URI.create(component.getUri().toString() + "#" + referenceName);
                for (URI uri : uris) {
                    if (refUri.equals(uri)) {
                        return true;
                    }
                }

            }
        }
        
        return false;

    }
    
    private ServiceContract<?> determineContract(LogicalReference reference) {
        
        ServiceContract<?> contract = reference.getDefinition().getServiceContract();
        if (contract != null) {
            return contract;
        }

        assert !reference.getPromotedUris().isEmpty();

        URI promotes = reference.getPromotedUris().get(0);
        URI defragmented = UriHelper.getDefragmentedName(promotes);
        String promotedReferenceName = promotes.getFragment();
        LogicalCompositeComponent parent = (LogicalCompositeComponent) reference.getParent();
        LogicalComponent<?> promotedComponent = parent.getComponent(defragmented);
        LogicalReference promotedReference;
        
        if (promotedReferenceName == null) {
            assert promotedComponent.getReferences().size() == 1;
            promotedReference = promotedComponent.getReferences().iterator().next();
        } else {
            promotedReference = promotedComponent.getReference(promotedReferenceName);
        }
        
        return determineContract(promotedReference);
        
    }

}
