/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.CompositeImplementation;

/**
 * Represents a composite component.
 */
public class LogicalCompositeComponent extends LogicalComponent<CompositeImplementation> {
    private static final long serialVersionUID = 6661201121307925462L;

    private final Map<LogicalReference, Set<LogicalWire>> wires = new HashMap<LogicalReference, Set<LogicalWire>>();
    private final Map<URI, LogicalComponent<?>> components = new HashMap<URI, LogicalComponent<?>>();

    /**
     * Instantiates a composite component.
     *
     * @param uri        URI of the component.
     * @param definition Definition of the component.
     * @param parent     Parent of the component.
     */
    public LogicalCompositeComponent(URI uri, ComponentDefinition<CompositeImplementation> definition, LogicalCompositeComponent parent) {
        super(uri, definition, parent);
    }

    /**
     * Adds a wire to this composite component.
     *
     * @param logicalReference the wire source
     * @param logicalWire      Wire to be added to this composite component.
     */
    public final void addWire(LogicalReference logicalReference, LogicalWire logicalWire) {
        Set<LogicalWire> logicalWires = wires.get(logicalReference);
        if (logicalWires == null) {
            logicalWires = new LinkedHashSet<LogicalWire>();
            wires.put(logicalReference, logicalWires);
        }
        logicalWires.add(logicalWire);
    }

    /**
     * Adds a set of wires to this composite component.
     *
     * @param logicalReference the source for the wires
     * @param logicalWires     the set of wires
     */
    public final void overrideWires(LogicalReference logicalReference, Set<LogicalWire> logicalWires) {
        wires.put(logicalReference, logicalWires);
    }

    /**
     * Gets the resolved targets sourced by the specified logical reference.
     *
     * @param logicalReference Logical reference that sources the wire.
     * @return Resolved targets for the reference.
     */
    public final Set<LogicalWire> getWires(LogicalReference logicalReference) {

        Set<LogicalWire> logicalWires = wires.get(logicalReference);
        if (logicalWires == null) {
            logicalWires = new LinkedHashSet<LogicalWire>();
            wires.put(logicalReference, logicalWires);
        }
        return logicalWires;

    }

    /**
     * Returns the child components of the current component.
     *
     * @return the child components of the current component
     */
    public Collection<LogicalComponent<?>> getComponents() {
        return components.values();
    }

    /**
     * Remove the child component based on the URI
     *
     * @param uri the component URI
     */
    public void removeComponent(URI uri) {
        components.remove(uri);
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

    @Override
    public void setState(LogicalState state) {
        super.setState(state);
        for (LogicalComponent<?> component : getComponents()) {
            component.setState(state);
        }
    }

}
