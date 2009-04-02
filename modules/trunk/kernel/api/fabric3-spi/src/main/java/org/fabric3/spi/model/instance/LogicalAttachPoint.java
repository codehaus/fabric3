/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.fabric3.model.type.service.Operation;
import org.fabric3.model.type.service.ServiceContract;

/**
 * Represents a contract-based attachment point on an instantiated component, specifically a service, reference, or resource.
 *
 * @version $Revision$ $Date$
 */
public class LogicalAttachPoint extends LogicalScaArtifact<LogicalComponent<?>> {
    private static final long serialVersionUID = -5294444649296282992L;
    protected List<LogicalOperation> operations;
    protected List<LogicalOperation> callbackOperations;

    /**
     * Constructor.
     *
     * @param uri      URI of the SCA artifact.
     * @param contract the service contract
     * @param parent   Parent of the SCA artifact.
     * @param type     Type of this artifact.
     */
    public LogicalAttachPoint(URI uri, ServiceContract<?> contract, LogicalComponent<?> parent, QName type) {
        super(uri, parent, type);
        operations = new ArrayList<LogicalOperation>();
        callbackOperations = new ArrayList<LogicalOperation>();
        if (contract != null) {
            // null is a convenience allowed for testing so the logical model does not need to be fully created
            for (Operation operation : contract.getOperations()) {
                operations.add(new LogicalOperation(operation, this));
            }
            ServiceContract<?> callbackContract = contract.getCallbackContract();
            if (callbackContract != null) {
                for (Operation<?> operation : callbackContract.getOperations()) {
                    callbackOperations.add(new LogicalOperation(operation, this));
                }
            }
        }
    }

    public List<LogicalOperation> getOperations() {
        return operations;
    }

    public List<LogicalOperation> getCallbackOperations() {
        return callbackOperations;
    }

}
