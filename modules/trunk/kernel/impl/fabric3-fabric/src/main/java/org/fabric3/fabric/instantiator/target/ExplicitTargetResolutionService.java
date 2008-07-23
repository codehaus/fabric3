package org.fabric3.fabric.instantiator.target;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.fabric.instantiator.AmbiguousService;
import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.fabric.instantiator.LogicalInstantiationException;
import org.fabric3.fabric.instantiator.NoServiceOnComponent;
import org.fabric3.fabric.instantiator.ServiceNotFound;
import org.fabric3.scdl.ComponentReference;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.util.UriHelper;

/**
 * Resolution based on an explicit target uri.
 *
 * @version $Revsion$ $Date$
 */
public class ExplicitTargetResolutionService implements TargetResolutionService {

    public void resolve(LogicalReference logicalReference, LogicalCompositeComponent component, LogicalChange change)
            throws LogicalInstantiationException {

        ComponentReference componentReference = logicalReference.getComponentReference();
        if (componentReference == null) {
            return;
        }

        List<URI> requestedTargets = componentReference.getTargets();
        if (requestedTargets.isEmpty()) {
            return;
        }

        URI parentUri = component.getUri();
        URI componentUri = logicalReference.getParent().getUri();

        List<URI> resolvedUris = new ArrayList<URI>();
        for (URI requestedTarget : requestedTargets) {
            URI resolved = parentUri.resolve(componentUri).resolve(requestedTarget);
            URI targetURI = resolveByUri(logicalReference, resolved, component, change);
            if (targetURI != null) {
                resolvedUris.add(targetURI);
            }
        }
        logicalReference.overrideTargets(resolvedUris);

    }

    private URI resolveByUri(LogicalReference reference, URI targetUri, LogicalCompositeComponent composite, LogicalChange change)
            throws LogicalInstantiationException {

        URI targetComponentUri = UriHelper.getDefragmentedName(targetUri);
        LogicalComponent<?> targetComponent = composite.getComponent(targetComponentUri);

        if (targetComponent == null) {
            throw new TargetComponentNotFoundException("Target component not found: " + targetComponentUri.toString()
                    + ". Originating reference is: " + reference.getUri());
        }

        String serviceName = targetUri.getFragment();
        if (serviceName != null) {
            if (targetComponent.getService(serviceName) == null) {
                String msg = "The service " + serviceName + " targeted from the reference " +reference.getUri() + " is not found on component " + UriHelper.getDefragmentedName(targetUri);
                ServiceNotFound error = new ServiceNotFound(msg, reference, targetComponentUri);
                change.addError(error);
                return null;
            }
            return targetUri;
        } else {
            LogicalService target = null;
            for (LogicalService service : targetComponent.getServices()) {
                if (service.getDefinition().isManagement()) {
                    continue;
                }
                if (target != null) {
                    String msg = "More than one service available on component: " + targetUri
                            + ". Reference must explicitly specify a target service: " + reference.getUri();
                    AmbiguousService error = new AmbiguousService(reference, msg);
                    change.addError(error);
                    return null;
                }
                target = service;
            }
            if (target == null) {
                String msg = "The reference " + reference.getUri() + " is wired to component "  + targetUri + " but the component has no services";
                NoServiceOnComponent error = new NoServiceOnComponent(msg, reference);
                change.addError(error);
                return null;
            }
            return target.getUri();
        }

    }

}
