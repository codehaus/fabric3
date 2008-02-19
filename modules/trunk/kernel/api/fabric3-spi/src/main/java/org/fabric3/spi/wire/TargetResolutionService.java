package org.fabric3.spi.wire;

import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;

/**
 * Abstraction for resolving targets for references. Possible implementations 
 * include explicit targets, intent based auto-wiring, type based auto-wiring 
 * etc.
 * 
 * @version $Revision$ $Date$
 *
 */
public interface TargetResolutionService {
    
    /**
     * Resolves the target for a logical reference.
     * 
     * @param reference Logical reference whose target needs to be resolved.
     * @param context Composite component within which the targets are resolved.
     * @return True is the target was succesfully involved.
     */
    void resolve(LogicalReference reference, LogicalCompositeComponent context) throws TargetResolutionException;
    
}
