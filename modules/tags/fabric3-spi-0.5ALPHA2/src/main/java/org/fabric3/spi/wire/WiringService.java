package org.fabric3.spi.wire;

import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Abstraction for resolving wires.
 *
 * @version $Revision$ $Date$
 */
public interface WiringService {

    /**
     * Wire the component handling promotions of services and references and resolving targets on references.
     *
     * @param logicalComponent Logical component that needs to be wired.
     */
    void wire(LogicalComponent<?> logicalComponent) throws ActivateException;

    /**
     * Handles the promotion on the specified logical service.
     *
     * @param logicalService Logical service whose promotion is handled.
     */
    void promote(LogicalService logicalService) throws PromotionException;

    /**
     * Resolves the target for a logical reference.
     *
     * @param logicalReference Logical reference whose target needs to be resolved.
     * @param context          Composite component within which the targets are resolved.
     * @return True is the target was succesfully involved.
     */
    void wire(LogicalReference logicalReference, LogicalCompositeComponent context) throws ActivateException;
}
