package org.fabric3.fabric.instantiator.target;

import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Resolves the service contract for services and references. Promoted services and references often do not specify a service contract explicitly,
 * instead using a contract defined further down in the promotion hierarchy. In these cases, the service contract is often inferred from the
 * implementation (e.g. a Java class) or explicitly declared within the component definition in a composite file.
 *
 * @version $Revision$ $Date$
 */
public interface ServiceContractResolver {

    /**
     * Returns the contract for a service.
     *
     * @param service the service to determine the service contract for.
     * @return the contract or null if none is found
     */
    ServiceContract<?> determineContract(LogicalService service);

    /**
     * Returns the contract for a reference.
     *
     * @param reference the reference to determine the service contract for.
     * @return the contract or null if none is found
     */
    ServiceContract<?> determineContract(LogicalReference reference);

}
