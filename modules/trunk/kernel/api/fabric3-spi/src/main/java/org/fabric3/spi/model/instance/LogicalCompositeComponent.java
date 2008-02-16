package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.CompositeImplementation;

/**
 * Represents a composite component.
 *
 */
public class LogicalCompositeComponent extends LogicalComponent<CompositeImplementation> {
    
    private final Map<LogicalReference, List<URI>> wires = new HashMap<LogicalReference, List<URI>>();
    private final Map<URI, LogicalComponent<?>> components = new HashMap<URI, LogicalComponent<?>>();

    /**
     * Instantiates a composite composite component.
     * 
     * @param uri URI of the component.
     * @param runtimeId Runtime id to which the component is provisioned.
     * @param definition Definition of the component.
     * @param parent Parent of the component.
     */
    public LogicalCompositeComponent(URI uri, 
                                     URI runtimeId, 
                                     ComponentDefinition<CompositeImplementation> definition, 
                                     LogicalCompositeComponent parent) {
        super(uri, runtimeId, definition, parent);
    }
    
    /**
     * Adds a wire to this composite component.
     * 
     * @param wire Wire to be added to this composite component.
     */
    public final void addWireTarget(LogicalReference logicalReference, URI target) {
        
        List<URI> targets = wires.get(logicalReference);
        if (targets == null) {
            targets = new LinkedList<URI>();
            wires.put(logicalReference, targets);
        }
        targets.add(target);
        
    }
    
    /**
     * Adds a wire to this composite component.
     * 
     * @param wire Wire to be added to this composite component.
     */
    public final void overrideWireTargets(LogicalReference logicalReference, List<URI> targets) {
        wires.put(logicalReference, targets);
    }
    
    /**
     * Gets the resolved targets sourced by the specified logical reference.
     * 
     * @param logicalReference Logical reference that sources the wire.
     * @return Resolved targets for the reference.
     */
    public final List<URI> getWireTargets(LogicalReference logicalReference) {
        
        List<URI> targets = wires.get(logicalReference);
        if (targets == null) {
            targets = new LinkedList<URI>();
            wires.put(logicalReference, targets);
        }
        return targets;
        
    }

    /**
     * Returns the child components of the current component.
     *
     * @return the child components of the current component
     */
    public Collection<LogicalComponent<?>> getComponents() {
        return Collections.unmodifiableCollection(components.values());
    }

    /**
     * Returns a child component with the given URI.
     *
     * @param uri the child component URI
     * @return a child component with the given URI.
     */
    public LogicalComponent<?> getComponent(URI uri) {
        return components.get(uri);
    }

    /**
     * Adds a child component
     *
     * @param component the child component to add
     */
    public void addComponent(LogicalComponent<?> component) {
        components.put(component.getUri(), component);
    }

}
