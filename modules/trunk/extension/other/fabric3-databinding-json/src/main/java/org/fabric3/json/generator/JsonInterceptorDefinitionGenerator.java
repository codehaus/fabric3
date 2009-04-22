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
package org.fabric3.json.generator;

import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.w3c.dom.Element;

import org.fabric3.model.type.component.Encodings;
import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.json.provision.JsonReferenceInterceptorDefinition;
import org.fabric3.json.provision.JsonServiceInterceptorDefinition;

/**
 * Generates interceptor definitions for operations marked with the JSON intent.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JsonInterceptorDefinitionGenerator implements InterceptorDefinitionGenerator {

    public PhysicalInterceptorDefinition generate(Element policySet, Operation<?> operation, LogicalBinding<?> logicalBinding)
            throws GenerationException {
        String encoding = logicalBinding.getDefinition().getEncoding();
        if (Encodings.JAVA.equals(encoding)) {
            // The binding does not use an encoding scheme so ignore.
            return null;
        }

        List<String> parameterClassNames = calculateParameterClassNames(operation);
        String returnClassNames = calculateReturnClassName(operation);
        List<String> faultClassNames = calculateFaultClassNames(operation);

        if (logicalBinding.getParent() instanceof LogicalService) {
            if (logicalBinding.isCallback()) {
                // callbacks on the service side of a wire take a reference interceptor since the callback invocation originates there
                return new JsonReferenceInterceptorDefinition(parameterClassNames, returnClassNames, faultClassNames);

            } else {
                return new JsonServiceInterceptorDefinition(parameterClassNames, returnClassNames, faultClassNames);
            }
        } else {
            if (logicalBinding.isCallback()) {
                // callbacks on the reference side of a wire take a service interceptor since the callback is received there
                return new JsonServiceInterceptorDefinition(parameterClassNames, returnClassNames, faultClassNames);
            } else {
                return new JsonReferenceInterceptorDefinition(parameterClassNames, returnClassNames, faultClassNames);
            }
        }
    }

    /**
     * @param operation the operation
     * @return the collated class names
     */
    private List<String> calculateParameterClassNames(Operation<?> operation) {
        List<String> classNames = new ArrayList<String>();
        List<? extends DataType<?>> inputTypes = operation.getInputType().getLogical();
        // parameter types
        for (DataType<?> inputType : inputTypes) {
            String className = ((Class<?>) inputType.getPhysical()).getName();
            classNames.add(className);
        }
        return classNames;
    }

    private String calculateReturnClassName(Operation<?> operation) {
        // return type
        DataType<?> returnType = operation.getOutputType();
        return ((Class<?>) returnType.getPhysical()).getName();
    }

    private List<String> calculateFaultClassNames(Operation<?> operation) {
        List<String> classNames = new ArrayList<String>();
        List<? extends DataType<?>> faultTypes = operation.getFaultTypes();
        for (DataType<?> faultType : faultTypes) {
            Class<?> faultClass = (Class<?>) faultType.getPhysical();
            classNames.add(faultClass.getName());
        }
        return classNames;
    }

}