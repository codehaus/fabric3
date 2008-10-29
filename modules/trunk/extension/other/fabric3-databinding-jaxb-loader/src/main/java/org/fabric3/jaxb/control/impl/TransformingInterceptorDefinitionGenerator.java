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

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.jaxb.control.api.JAXBTransformationService;
import org.fabric3.jaxb.provision.AbstractTransformingInterceptorDefinition;
import org.fabric3.jaxb.provision.ReferenceTransformingInterceptorDefinition;
import org.fabric3.jaxb.provision.ServiceTransformingInterceptorDefinition;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import org.fabric3.spi.Namespaces;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalService;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;
import org.w3c.dom.Element;

/**
 * Generates interceptor definitions for operations marked with the JAXB intent.
 *
 * @version $Revision$ $Date$
 */
@Service(interfaces = {InterceptorDefinitionGenerator.class, JAXBTransformationService.class})
@EagerInit
public class TransformingInterceptorDefinitionGenerator implements InterceptorDefinitionGenerator, JAXBTransformationService {
    private static final QName INTENT_QNAME = new QName(Namespaces.POLICY, "jaxbPolicy");

    private GeneratorRegistry generatorRegistry;
    private Map<QName, QName> engagedBindings;

    public TransformingInterceptorDefinitionGenerator(@Reference GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
        engagedBindings = new HashMap<QName, QName>();
    }

    @Init
    public void init() {
        generatorRegistry.register(INTENT_QNAME, this);
    }

    public void registerBinding(QName name, QName dataType) {
        engagedBindings.put(name, dataType);
    }

    public AbstractTransformingInterceptorDefinition generate(Element policySet, Operation<?> operation, LogicalBinding<?> logicalBinding)
            throws GenerationException {
        QName dataType = engagedBindings.get(logicalBinding.getBinding().getType());
        if (dataType == null) {
            // The binding does not use JAXB, ignore. For example, a collocated wire may pass JAXB types but they do not need to be serialized
            // as invocations flow through the same VM.
            return null;
        }

        URI classLoaderId = logicalBinding.getParent().getParent().getClassLoaderId();
        Set<String> classNames = calculateParameterClassNames(operation);

        if (logicalBinding.getParent() instanceof LogicalService) {
            return new ServiceTransformingInterceptorDefinition(classLoaderId, dataType, classNames);
        } else {
            return new ReferenceTransformingInterceptorDefinition(classLoaderId, dataType, classNames);
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