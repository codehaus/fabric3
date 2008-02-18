package org.fabric3.spi.wire;

import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Abstraction for promoting services and references.
 * 
 * @version $Revision$ $Date$
 *
 */
public interface PromotionService {
    
    /**
     * Handles the promotion on the specified logical service. 
     * 
     * @param logicalService Logical service whose promotion is handled.
     */
    void promote(LogicalService logicalService) throws PromotionException;
    
    /**
     * Handles all the promotions on the specified logical reference. 
     * 
     * @param logicalReference Logical reference whose promotion is handled.
     */
    void promote(LogicalReference logicalReference) throws PromotionException;
    
}
