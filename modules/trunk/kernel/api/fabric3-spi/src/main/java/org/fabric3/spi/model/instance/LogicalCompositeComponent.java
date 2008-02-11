package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.CompositeImplementation;

/**
 * Represents a composite component.
 *
 */
public class LogicalCompositeComponent extends LogicalComponent<CompositeImplementation> {
    
    private final Set<LogicalWire> wires = new HashSet<LogicalWire>();
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
     * Gets the list of wires resolved within this composite component.
     * 
     * @return List of wires resolved within this composite component.
     */
    public final Set<LogicalWire> getWires() {
        return Collections.unmodifiableSet(wires);
    }
    
    /**
     * Adds a wire to this composite component.
     * 
     * @param wire Wire to be added to this composite component.
     */
    public final void addWire(LogicalWire wire) {
        wires.add(wire);
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
