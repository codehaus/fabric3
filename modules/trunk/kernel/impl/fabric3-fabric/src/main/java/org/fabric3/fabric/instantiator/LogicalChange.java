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
package org.fabric3.fabric.instantiator;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.host.domain.AssemblyFailure;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * @version $Rev$ $Date$
 */
public class LogicalChange {

    private final LogicalCompositeComponent parent;

    private final List<AssemblyFailure> errors = new ArrayList<AssemblyFailure>();

    /**
     * Construct a logical change specifiying the context to which it applies.
     *
     * @param parent the context to which this change applies
     */
    public LogicalChange(LogicalCompositeComponent parent) {
        this.parent = parent;
    }

    /**
     * Returns the component the change is targeted at.
     *
     * @return the component the change is targeted at.
     */
    public LogicalCompositeComponent getParent() {
        return parent;
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
    public List<AssemblyFailure> getErrors() {
        return errors;
    }

    /**
     * Add a fatal error to the chnage.
     *
     * @param error the fatal error that has been found
     */
    public void addError(AssemblyFailure error) {
        errors.add(error);
    }

    /**
     * Add a collection of fatal errors to the change.
     *
     * @param errors the fatal errors that have been found
     */
    public void addErrors(List<AssemblyFailure> errors) {
        this.errors.addAll(errors);
    }

}
