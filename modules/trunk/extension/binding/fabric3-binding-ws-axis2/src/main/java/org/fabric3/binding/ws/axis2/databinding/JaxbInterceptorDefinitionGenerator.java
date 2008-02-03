/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.binding.ws.axis2.databinding;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Operation;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalService;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class JaxbInterceptorDefinitionGenerator implements InterceptorDefinitionGenerator {

    private static final QName EXTENSION_NAME = new QName("http://fabric3.org/xmlns/sca/2.0-alpha/axis", "dataBinding.jaxb");
    
    private GeneratorRegistry generatorRegistry;
    private ClassLoaderGenerator classLoaderGenerator;

    public JaxbInterceptorDefinitionGenerator(@Reference GeneratorRegistry generatorRegistry, @Reference ClassLoaderGenerator classLoaderGenerator) {
        this.generatorRegistry = generatorRegistry;
        this.classLoaderGenerator = classLoaderGenerator;
    }

    /**
     * Registers with the registry.
     */
    @Init
    public void init() {
        generatorRegistry.register(EXTENSION_NAME, this);
    }

    @SuppressWarnings("unchecked")
    public JaxbInterceptorDefinition generate(Element policySet, 
                                              GeneratorContext generatorContext,
                                              Operation<?> operation,
                                              LogicalBinding<?> logicalBinding) throws GenerationException {
        
        boolean service = logicalBinding.getParent() instanceof LogicalService;
        
        URI classLoaderId = classLoaderGenerator.generate(logicalBinding, generatorContext);
        
        // This assumes a Java interface contract
        Operation<Type> javaOperation = (Operation<Type>) operation;
        
        List<DataType<Type>> inputTypes = javaOperation.getInputType().getLogical();
        DataType<Type> outputType = javaOperation.getOutputType();
        
        List<String> inClassNames = new LinkedList<String>();
        
        for (DataType<Type> inputType : inputTypes) {
            String inClassName = ((Class) inputType.getLogical()).getName();
            inClassNames.add(inClassName);
        }
        
        String outClassName = ((Class) outputType.getLogical()).getName();
        
        return new JaxbInterceptorDefinition(classLoaderId, inClassNames, outClassName, service);
    }

}
