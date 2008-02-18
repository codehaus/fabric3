package org.fabric3.fabric.wire;

import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.wire.TargetResolutionException;
import org.fabric3.spi.wire.TargetResolutionService;

/**
 * Resolution based on intent based auto-wire.
 * 
 * @version $Revsion$ $Date$
 *
 */
public class IntentBasedAutoWireService implements TargetResolutionService {

    public boolean resolve(LogicalReference reference, LogicalCompositeComponent context) throws TargetResolutionException {
        return true;
    }

}
