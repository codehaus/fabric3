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
package org.fabric3.fabric.model.logical;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * @version $Rev$ $Date$
 */
public class LogicalChange {

    private final LogicalCompositeComponent parent;
    private final List<Command> phase1 = new ArrayList<Command>();
    private final List<Command> phase2 = new ArrayList<Command>();
    private final List<Command> phase3 = new ArrayList<Command>();

    private final List<String> deletedProperties = new ArrayList<String>();
    private final List<LogicalComponent<?>> deletedComponents =
            new ArrayList<LogicalComponent<?>>();
    
    private final List<LogicalComponent<?>> addedComponents =
            new ArrayList<LogicalComponent<?>>();

    /**
     * Construct change specifiying the context to which it applies.
     *
     * @param parent the context to which this change applies
     */
    public LogicalChange(LogicalCompositeComponent parent) {
        this.parent = parent;
    }

    /**
     * Apply this change to its context.
     */
    public void apply() {
        for (Command command : phase1) {
            command.apply();
        }
        for (Command command : phase2) {
            command.apply();
        }
        for (Command command : phase3) {
            command.apply();
        }
    }

    /**
     * Change that adds a property to the parent context.
     *
     * @param name  the name of the property to add
     * @param value the actual value of the property
     */
    public void addProperty(final String name, final Document value) {
        phase1.add(new Command() {
            public void apply() {
                parent.setPropertyValue(name, value);
            }
        });
    }

    public void removeProperty(final String name) {
        deletedProperties.add(name);
    }

    /**
     * Change that adds a component to the parent context.
     *
     * @param component the component to add
     */
    public void addComponent(final LogicalComponent<?> component) {
        phase2.add(new Command() {
            public void apply() {
                parent.addComponent(component);
            }
        });
        addedComponents.add(component);
    }

    /**
     * Change that removes a component from the parent context
     * @param component
     */
    public void removeComponent(final LogicalComponent<?> component) {
        deletedComponents.add(component);
    }

    /**
     * Return the list of new components added to the parent context
     * @return
     */
    public List<LogicalComponent<?>> getAddedComponents() {
        return addedComponents;
    }

    /**
     * Return the list of deleted components from the parent context
      * @return
     */

    public List<LogicalComponent<?>> getDeletedComponents() {
        return deletedComponents;
    }

    /**
     * Change that adds a wire to the parent context.
     *
     * @param reference the reference that sources the wire
     * @param wire      the wire
     */
    public void addWire(final LogicalReference reference, final LogicalWire wire) {
        phase3.add(new Command() {
            public void apply() {
                parent.addWire(reference, wire);
            }
        });
    }

    private static abstract class Command {
        public abstract void apply();
    }
}
