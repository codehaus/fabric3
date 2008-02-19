package org.fabric3.fabric.wire.autowire;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.wire.TargetResolutionException;
import org.fabric3.spi.wire.TargetResolutionService;

/**
 * Resolution based on type based auto-wire.
 * 
 * @version $Revsion$ $Date$
 *
 */
public class ExplicitTargetResolutionService implements TargetResolutionService {

    public void resolve(LogicalReference reference, LogicalCompositeComponent context) throws TargetResolutionException {
        
        ReferenceDefinition referenceDefinition = reference.getDefinition();
        LogicalComponent<?> parentComponent = reference.getParent();
        
        ComponentDefinition<?> componentDefinition = parentComponent.getDefinition();
        Map<String, ComponentReference> explicitReferences = componentDefinition.getReferences();
        
        String referenceName = referenceDefinition.getName();
        if (!explicitReferences.containsKey(referenceName)) {
            return;
        }
        
        List<URI> requestedTargets = explicitReferences.get(referenceName).getTargets();
        if (requestedTargets.isEmpty()) {
            return;
        }
        
    }

}
