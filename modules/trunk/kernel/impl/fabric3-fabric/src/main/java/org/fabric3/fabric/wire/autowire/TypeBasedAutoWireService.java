package org.fabric3.fabric.wire.autowire;

import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.wire.TargetResolutionException;
import org.fabric3.spi.wire.TargetResolutionService;

/**
 * Resolution based on an explicitly requested target.
 * 
 * @version $Revsion$ $Date$
 *
 */
public class TypeBasedAutoWireService implements TargetResolutionService {

    public boolean resolve(LogicalReference reference, LogicalCompositeComponent context) throws TargetResolutionException {
        return true;
    }

}
