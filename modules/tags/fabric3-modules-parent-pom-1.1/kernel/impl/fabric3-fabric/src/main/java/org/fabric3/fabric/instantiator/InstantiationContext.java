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
 * A context used during logical component instantiation. Used primarily to record errors.
 *
 * @version $Rev$ $Date$
 */
public class InstantiationContext {

    private final LogicalCompositeComponent parent;

    private final List<AssemblyFailure> errors = new ArrayList<AssemblyFailure>();

    /**
     * Constructor.
     *
     * @param parent the target composite an instantiation is being made to.
     */
    public InstantiationContext(LogicalCompositeComponent parent) {
        this.parent = parent;
    }

    /**
     * Returns the component instantiation operation is targeted at.
     *
     * @return the component the instantiation operation is targeted at.
     */
    public LogicalCompositeComponent getParent() {
        return parent;
    }

    /**
     * Returns true if the instantiation operation detected any fatal errors.
     *
     * @return true if the instantiation operation has detected any fatal errors
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Returns the list of fatal errors detected during the instantiation operation.
     *
     * @return the list of fatal errors detected during the instantiation operation
     */
    public List<AssemblyFailure> getErrors() {
        return errors;
    }

    /**
     * Add a fatal error to the instantiation context.
     *
     * @param error the fatal error that has been found
     */
    public void addError(AssemblyFailure error) {
        errors.add(error);
    }

    /**
     * Add a collection of fatal errors to the instantiation context.
     *
     * @param errors the fatal errors that have been found
     */
    public void addErrors(List<AssemblyFailure> errors) {
        this.errors.addAll(errors);
    }

}
