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
import org.fabric3.spi.wire.PromotionException;
import org.fabric3.spi.wire.TargetResolutionService;

/**
 * Resolution based on type based auto-wire.
 *
 * @version $Revsion$ $Date$
 */
public class ExplicitTargetResolutionService implements TargetResolutionService {

    public void resolve(LogicalReference logicalReference, LogicalCompositeComponent context) throws PromotionException {

        ComponentReference componentReference = logicalReference.getComponentReference();
        if (componentReference == null) {
            return;
        }

        List<URI> requestedTargets = componentReference.getTargets();
        if (requestedTargets.isEmpty()) {
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

    }

    private URI resolveByUri(LogicalReference reference, URI targetUri, LogicalCompositeComponent composite) throws PromotionException {

        URI targetComponentUri = UriHelper.getDefragmentedName(targetUri);
        LogicalComponent<?> targetComponent = composite.getComponent(targetComponentUri);

        if (targetComponent == null) {
            throw new TargetComponentNotFoundException("Target component not found: " + targetComponentUri.toString()
                    + ". Originating reference is: " + reference.getUri());
        }

        String serviceName = targetUri.getFragment();
        if (serviceName != null) {
            if (targetComponent.getService(serviceName) == null) {
                throw new ServiceNotFoundException("Service " + serviceName + " not found on component: "
                        + UriHelper.getDefragmentedName(targetUri) + ". Originating reference is: " + reference.getUri());
            }
            return targetUri;
        } else {
            LogicalService target = null;
            for (LogicalService service : targetComponent.getServices()) {
                if (service.getDefinition().isManagement()) {
                    continue;
                }
                if (target != null) {
                    throw new AmbiguousServiceException("More than one service available on component: " + targetUri
                            + ". Reference must explicitly specify a target service: " + reference.getUri());
                }
                target = service;
            }
            if (target == null) {
                throw new NoServiceOnComponentException("No services available on component: "
                        + targetUri + ". Originating reference is: " + reference.getUri());
            }
            return target.getUri();
        }

    }

}
