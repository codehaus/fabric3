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
package org.fabric3.policy.xpath;

import java.util.ArrayList;
import java.util.List;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Implements the OperationRef function defined by the SCA Policy Specificaton.
 *
 * @version $Revision$ $Date$
 */
public class OperationRefFunction implements Function {

    @SuppressWarnings({"unchecked"})
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() != 1) {
            throw new FunctionCallException("Invalid number of arguments for OperationRef(): " + args.size());
        }
        Object arg = args.get(0);
        String[] tokens = arg.toString().split("/");
        if (tokens.length != 2) {
            throw new FunctionCallException("Invalid Service/Operation name: " + arg);
        }
        String interfaceName = tokens[0];
        String operationName = tokens[1];
        List<LogicalComponent<?>> nodeSet = context.getNodeSet();
        List<LogicalOperation> operations = new ArrayList<LogicalOperation>();
        for (LogicalComponent<?> component : nodeSet) {
            find(interfaceName, operationName, component, operations);
        }
        return operations;
    }

    private void find(String interfaceName, String operationName, LogicalComponent<?> component, List<LogicalOperation> operations) {
        for (LogicalService service : component.getServices()) {
            ServiceDefinition definition = service.getDefinition();
            ServiceContract<?> contract = definition.getServiceContract();
            // match on the name of the service contract but return the logical operation
            if (contract.getInterfaceName().equals(interfaceName)) {
                for (LogicalOperation operation : service.getOperations()) {
                    if (operation.getDefinition().getName().equals(operationName)) {
                        operations.add(operation);
                    }
                }
            }
        }
        for (LogicalReference reference : component.getReferences()) {
            ReferenceDefinition definition = reference.getDefinition();
            // match on the name of the service contract but return the logical operation
            ServiceContract<?> contract = definition.getServiceContract();
            if (contract.getInterfaceName().equals(interfaceName)) {
                for (LogicalOperation operation : reference.getOperations()) {
                    if (operation.getDefinition().getName().equals(operationName)) {
                        operations.add(operation);
                    }
                }
            }
        }
        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : composite.getComponents()) {
                find(interfaceName, operationName, child, operations);
            }
        }

    }
}