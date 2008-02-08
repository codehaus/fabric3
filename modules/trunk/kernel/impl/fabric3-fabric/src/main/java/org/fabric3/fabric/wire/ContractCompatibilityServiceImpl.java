package org.fabric3.fabric.wire;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class ContractCompatibilityServiceImpl implements ContractCompatibilityService {

    public boolean checkCompatibility(ServiceContract<?> source,
                                      ServiceContract<?> target,
                                      boolean silent)
            throws IncompatibleServiceContractException {
        if (source == target) {
            // Shortcut for performance
            return true;
        }
        if (source.isRemotable() != target.isRemotable()) {
            if (!silent) {
                throw new IncompatibleServiceContractException("Remotable settings do not match", source, target);
            } else {
                return false;
            }
        }
        if (source.isConversational() != target.isConversational()) {
            if (!silent) {
                throw new IncompatibleServiceContractException("Interaction scopes do not match", source, target);
            } else {
                return false;
            }
        }

        for (Operation<?> operation : source.getOperations()) {
            // FIXME support overloading
            Operation<?> targetOperation = null;
            for (Operation<?> o : target.getOperations()) {
                if (o.getName().equals(operation.getName())) {
                    targetOperation = o;
                    break;
                }
            }
            if (targetOperation == null) {
                if (!silent) {
                    throw new IncompatibleServiceContractException("Operation not found on target", source, target);
                } else {
                    return false;
                }
            }
            if (!operation.equals(targetOperation)) {
                if (!silent) {
                    throw new IncompatibleServiceContractException("Target operations are not compatible", source,
                                                                   target);
                } else {
                    return false;
                }
            }
        }

        // TODO JFM add callback check
        return true;
    }


}
