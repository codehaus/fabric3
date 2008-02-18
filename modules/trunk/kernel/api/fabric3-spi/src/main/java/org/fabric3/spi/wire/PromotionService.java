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
     * @param logicalService
     */
    void promote(LogicalService logicalService) throws PromotionException;
    
    /**
     * @param logicalReference
     */
    void promote(LogicalReference logicalReference) throws PromotionException;
    
}
