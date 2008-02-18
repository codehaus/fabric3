package org.fabric3.spi.wire;

import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Abstraction for resolving wires.
 * 
 * @version $Revision$ $Date$
 *
 */
public interface WiringService {
    
    /**
     * Wire the component handling promotions of services and references 
     * and resolving targets on references.
     * 
     * @param logicalComponent Logical component that needs to be wired.
     * @throws WiringException If unable to wire the component.
     */
    void wire(LogicalComponent<?> logicalComponent) throws WiringException;
}
