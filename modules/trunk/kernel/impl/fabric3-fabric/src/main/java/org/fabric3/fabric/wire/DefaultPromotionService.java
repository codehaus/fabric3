package org.fabric3.fabric.wire;

import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.wire.PromotionException;
import org.fabric3.spi.wire.PromotionService;

/**
 * Default implementation of the promotion service.
 * 
 * @version $Revision$ $Date$
 *
 */
public class DefaultPromotionService implements PromotionService {

    public void promote(LogicalService logicalService) throws PromotionException {
    }

    public void promote(LogicalReference logicalReference) throws PromotionException {
    }

}
