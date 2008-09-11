package org.fabric3.fabric.instantiator.target;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.util.UriHelper;

/**
 * @version $Revision$ $Date$
 */
public class ServiceContractResolverImpl implements ServiceContractResolver {

    public ServiceContract<?> determineContract(LogicalService service) {
        ServiceContract<?> contract = service.getDefinition().getServiceContract();
        if (contract != null) {
            return contract;
        }
        if (!(service.getParent() instanceof LogicalCompositeComponent)) {
            return null;
        }
        LogicalCompositeComponent parent = (LogicalCompositeComponent) service.getParent();
        URI promotedUri = service.getPromotedUri();
        LogicalComponent<?> promoted = parent.getComponent(UriHelper.getDefragmentedName(promotedUri));
        assert promoted != null;
        String serviceName = promotedUri.getFragment();
        LogicalService promotedService;
        if (serviceName == null && promoted.getServices().size() == 1) {
            // select the default service as a service name was not specified
            Collection<LogicalService> services = promoted.getServices();
            promotedService = services.iterator().next();
        } else if (serviceName == null) {
            // programing error
            throw new AssertionError("Service must be specified");
        } else {
            promotedService = promoted.getService(serviceName);
        }
        if (promotedService == null) {
            throw new AssertionError("Promoted service was null");
        }
        return determineContract(promotedService);
    }

    public ServiceContract<?> determineContract(LogicalReference reference) {
        ServiceContract<?> contract = reference.getDefinition().getServiceContract();
        if (contract != null) {
            return contract;
        }
        if (!(reference.getParent() instanceof LogicalCompositeComponent)) {
            return null;
        }
        LogicalCompositeComponent parent = (LogicalCompositeComponent) reference.getParent();
        //URI promotedUri = service.getPromotedUri();
        List<URI> promotedUris = reference.getPromotedUris();
        if (promotedUris.size() < 1) {
            // this is an invalid configuration: a reference with no service contract that does not promote another reference and should be
            // caught during the load phase before reaching here.
            throw new AssertionError();
        }
        // pick the first one since references expose the same contract
        URI promotedUri = promotedUris.get(0);

        LogicalComponent<?> promoted = parent.getComponent(UriHelper.getDefragmentedName(promotedUri));
        assert promoted != null;
        String referenceName = promotedUri.getFragment();
        LogicalReference promotedReference;
        if (referenceName == null && promoted.getReferences().size() == 1) {
            // select the default reference as a reference name was not specified
            Collection<LogicalReference> references = promoted.getReferences();
            promotedReference = references.iterator().next();
        } else if (referenceName == null) {
            // programing error
            throw new AssertionError("Reference must be specified");
        } else {
            promotedReference = promoted.getReference(referenceName);
        }
        if (promotedReference == null) {
            throw new AssertionError("Promoted reference was null");
        }
        return determineContract(promotedReference);
    }

}
