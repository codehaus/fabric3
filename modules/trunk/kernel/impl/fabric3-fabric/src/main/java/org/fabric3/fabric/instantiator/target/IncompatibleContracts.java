package org.fabric3.fabric.instantiator.target;

import java.net.URI;

import org.fabric3.host.domain.AssemblyFailure;

/**
 * Thrown when an attempt is made to wire a reference to a service with incompatible contracts.
 *
 * @version $Revision$ $Date$
 */
public class IncompatibleContracts extends AssemblyFailure {
    private URI referenceUri;
    private URI serviceUri;

    /**
     * Constructor.
     *
     * @param componentUri the URI of the component associated with the failure.
     * @param referenceUri the URI of the reference
     * @param serviceUri   the URI of the service
     */
    public IncompatibleContracts(URI componentUri, URI referenceUri, URI serviceUri) {
        super(componentUri);
        this.referenceUri = referenceUri;
        this.serviceUri = serviceUri;
    }

    public String getMessage() {
        return "The contracts for the reference " + referenceUri + " and service " + serviceUri + " are incompatible";
    }
}
