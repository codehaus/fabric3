/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/

package org.fabric3.binding.ws.metro.generator;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jws.WebMethod;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.fabric3.binding.ws.metro.provision.PolicyExpressionMapping;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.policy.EffectivePolicy;

/**
 * @version $Rev$ $Date$
 */
public class GenerationHelper {
    private static final String WS_SECURITY_UTILITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    private GenerationHelper() {
    }

    /**
     * Maps policy expressions to the operations they are attached to.
     *
     * @param serviceClass the service endpoint class
     * @param policy       the policy for the wire
     * @return the policy expression mappings
     * @throws GenerationException if the policy expression is invalid
     */
    public static List<PolicyExpressionMapping> createMappings(EffectivePolicy policy, Class<?> serviceClass) throws GenerationException {
        // temporarily store mappings keyed by policy expression id
        Map<String, PolicyExpressionMapping> mappings = new HashMap<String, PolicyExpressionMapping>();
        for (Map.Entry<LogicalOperation, List<PolicySet>> entry : policy.getOperationPolicySets().entrySet()) {
            Operation definition = entry.getKey().getDefinition();
            for (PolicySet policySet : entry.getValue()) {
                Element expression = policySet.getExpression();
                Node node = expression.getAttributes().getNamedItemNS(WS_SECURITY_UTILITY_NS, "Id");
                if (node == null) {
                    URI uri = policySet.getContributionUri();
                    QName expressionName = policySet.getExpressionName();
                    throw new GenerationException("Invalid policy in contribution " + uri + ". No id specified: " + expressionName);
                }
                String id = node.getNodeValue();

                PolicyExpressionMapping mapping = mappings.get(id);
                if (mapping == null) {
                    mapping = new PolicyExpressionMapping(id, expression);
                    mappings.put(id, mapping);
                }
                String operationName = getWsdlName(definition, serviceClass);
                mapping.addOperationName(operationName);
            }
        }
        return new ArrayList<PolicyExpressionMapping>(mappings.values());
    }

    /**
     * Returns the WSDL name for an operation following JAX-WS rules. Namely, if present the <code>@WebMethod.operationName()</code> attribute value
     * is used, otherwise the default operation name is returned.
     *
     * @param operation    the operation definition
     * @param serviceClass the implementation class
     * @return the WSDL operation name
     */
    private static String getWsdlName(Operation operation, Class<?> serviceClass) {
        Method method = findMethod(operation, serviceClass);
        WebMethod annotation = method.getAnnotation(WebMethod.class);
        if (annotation == null || annotation.operationName().length() < 1) {
            return operation.getName();
        }
        return annotation.operationName();
    }

    /**
     * Returns a Method corresponding to the operation definition on a service implementation class.
     *
     * @param operation    the operation definition
     * @param serviceClass the implementation class
     * @return the method
     */
    @SuppressWarnings({"unchecked"})
    private static Method findMethod(Operation operation, Class<?> serviceClass) {
        List<DataType<?>> types = operation.getInputTypes();
        Class<?>[] params = new Class<?>[types.size()];
        for (int i = 0; i < types.size(); i++) {
            DataType<?> type = types.get(i);
            params[i] = type.getPhysical();
        }
        try {
            return serviceClass.getMethod(operation.getName(), params);
        } catch (NoSuchMethodException e) {
            // should not happen
            throw new AssertionError(e);
        }
    }


}
