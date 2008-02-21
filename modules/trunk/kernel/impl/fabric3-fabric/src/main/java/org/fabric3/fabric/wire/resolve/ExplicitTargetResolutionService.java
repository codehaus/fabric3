package org.fabric3.fabric.wire.resolve;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.scdl.ComponentReference;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.TargetResolutionException;
import org.fabric3.spi.wire.TargetResolutionService;

/**
 * Resolution based on type based auto-wire.
 * 
 * @version $Revsion$ $Date$
 *
 */
public class ExplicitTargetResolutionService implements TargetResolutionService {

    public void resolve(LogicalReference logicalReference, LogicalCompositeComponent context) throws TargetResolutionException {
        
        ComponentReference componentReference = logicalReference.getComponentReference();
        if (componentReference == null) {
            return;
        }
        
        List<URI> requestedTargets = componentReference.getTargets();
        if (!requestedTargets.isEmpty()) {
            return;
        }
        
        URI parentUri = context.getUri();
        URI componentUri = logicalReference.getParent().getUri();
        
        List<URI> resolvedUris = new ArrayList<URI>();
        for (URI requestedTarget : requestedTargets) {
            URI resolved = parentUri.resolve(componentUri).resolve(requestedTarget);
            URI targetURI = resolveByUri(logicalReference, resolved, context);
            resolvedUris.add(targetURI);
        }
        logicalReference.overrideTargets(resolvedUris);
        
        return;
        
    }
    
    private URI resolveByUri(LogicalReference reference, URI targetUri, LogicalCompositeComponent composite) throws TargetResolutionException {
        
        URI targetComponentUri = UriHelper.getDefragmentedName(targetUri);
        LogicalComponent<?> targetComponent = composite.getComponent(targetComponentUri);
        
        if (targetComponent == null) {
            throw new TargetComponentNotFoundException(targetUri);
        }
        
        String serviceName = targetUri.getFragment();
        if (serviceName != null) {
            if (targetComponent.getService(serviceName) == null) {
                throw new ServiceNotFoundException(targetUri, serviceName);
            }
            return targetUri;
        } else {
            if (targetComponent.getServices().size() != 1) {
                if (targetComponent.getServices().size() > 1) {
                    throw new AmbiguousServiceException(targetUri);
                } else {
                    throw new NoServiceOnComponentException(targetUri);
                }
            }
            LogicalService targetService = targetComponent.getServices().iterator().next();
            return targetService.getUri();
        }
        
    }

}
