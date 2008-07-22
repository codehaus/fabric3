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
package org.fabric3.fabric.instantiator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * @version $Rev$ $Date$
 */
public class LogicalChange {

    private final LogicalCompositeComponent parent;
    private final List<Command> phase1 = new ArrayList<Command>();
    private final List<Command> phase2 = new ArrayList<Command>();
    private final List<Command> phase3 = new ArrayList<Command>();

    private final List<LogicalChangeFailure<?>> errors = new ArrayList<LogicalChangeFailure<?>>();
    private final List<LogicalChangeFailure<?>> warnings = new ArrayList<LogicalChangeFailure<?>>();

    private final List<String> deletedProperties = new ArrayList<String>();
    private final List<LogicalComponent<?>> deletedComponents = new ArrayList<LogicalComponent<?>>();
    private final List<LogicalComponent<?>> addedComponents = new ArrayList<LogicalComponent<?>>();
    private final List<URI> deletedServices = new ArrayList<URI>();

    /**
     * Construct change specifiying the context to which it applies.
     *
     * @param parent the context to which this change applies
     */
    public LogicalChange(LogicalCompositeComponent parent) {
        this.parent = parent;
    }

    /**
     * Returns true if the change generation has detected any fatal errors.
     *
     * @return true if the change generation has detected any fatal errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns the list of fatal errors detected during change generation.
     *
     * @return the list of fatal errors detected during change generation
     */
    public List<LogicalChangeFailure<?>> getErrors() {
        return errors;
    }

    /**
     * Add a fatal error to the chnage.
     *
     * @param error the fatal error that has been found
     */
    public void addError(LogicalChangeFailure<?> error) {
        errors.add(error);
    }

    /**
     * Add a collection of fatal errors to the change.
     *
     * @param errors the fatal errors that have been found
     */
    public void addErrors(List<LogicalChangeFailure<?>> errors) {
        this.errors.addAll(errors);
    }

    /**
     * Returns true if the change generation has detected any non-fatal warnings.
     *
     * @return true if the change generation has detected any non-fatal warnings
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * Returns the list of non-fatal warnings detected during change generation.
     *
     * @return the list of non-fatal warnings detected during change generation
     */
    public List<LogicalChangeFailure<?>> getWarnings() {
        return warnings;
    }

    /**
     * Add a non-fatal warning to the change.
     *
     * @param warning the non-fatal warning that has been found
     */
    public void addWarning(LogicalChangeFailure<?> warning) {
        warnings.add(warning);
    }

    /**
     * Add a collection of non-fatal warnings to the change.
     *
     * @param warnings the non-fatal warnings that have been found
     */
    public void addWarnings(List<LogicalChangeFailure<?>> warnings) {
        this.warnings.addAll(warnings);
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
     *
     * @param component
     */
    public void removeComponent(final LogicalComponent<?> component) {
        deletedComponents.add(component);
    }


    public void removeService(URI uri) {
        deletedServices.add(uri);
    }

    /**
     * Return the list of new components added to the parent context
     *
     * @return
     */
    public List<LogicalComponent<?>> getAddedComponents() {
        return addedComponents;
    }

    /**
     * Return the list of deleted components from the parent context
     *
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
