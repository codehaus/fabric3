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
package org.fabric3.jaxb.control.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osoa.sca.annotations.EagerInit;
import org.w3c.dom.Element;

import org.fabric3.jaxb.provision.AbstractTransformingInterceptorDefinition;
import org.fabric3.jaxb.provision.ReferenceTransformingInterceptorDefinition;
import org.fabric3.jaxb.provision.ServiceTransformingInterceptorDefinition;
import org.fabric3.model.type.component.Encodings;
import org.fabric3.model.type.service.DataType;
import org.fabric3.model.type.service.Operation;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Generates interceptor definitions for operations marked with the JAXB intent.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class TransformingInterceptorDefinitionGenerator implements InterceptorDefinitionGenerator {

    public AbstractTransformingInterceptorDefinition generate(Element policySet, Operation<?> operation, LogicalBinding<?> logicalBinding)
            throws GenerationException {
        String encoding = logicalBinding.getDefinition().getEncoding();
        if (Encodings.JAVA.equals(encoding)) {
            // The binding does not use an encoding scheme so ignore.
            return null;
        }

        Set<String> classNames = calculateParameterClassNames(operation);

        if (logicalBinding.getParent() instanceof LogicalService) {
            return new ServiceTransformingInterceptorDefinition(encoding, classNames);
        } else {
            return new ReferenceTransformingInterceptorDefinition(encoding, classNames);
        }
    }

    /**
     * Collates classnames for in and out parameters, faults, and return types on an operation.
     *
     * @param operation the operation
     * @return the collated class names
     */
    private Set<String> calculateParameterClassNames(Operation<?> operation) {
        Set<String> classNames = new HashSet<String>();
        List<? extends DataType<?>> inputTypes = operation.getInputType().getLogical();
        // parameter types
        for (DataType<?> inputType : inputTypes) {
            String className = ((Class<?>) inputType.getPhysical()).getName();
            classNames.add(className);
        }

        // fault types
        List<? extends DataType<?>> faultTypes = operation.getFaultTypes();
        for (DataType<?> faultType : faultTypes) {
            Class<?> faultClass = (Class<?>) faultType.getPhysical();
            classNames.add(faultClass.getName());
        }

        // return type
        DataType<?> returnType = operation.getOutputType();
        classNames.add(((Class<?>) returnType.getPhysical()).getName());
        return classNames;
    }

}