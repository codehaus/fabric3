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

import javax.xml.namespace.QName;

import org.fabric3.spi.Constants;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * @version $Revision$ $Date$
 */
public class JaxbInterceptorDefinitionGenerator implements InterceptorDefinitionGenerator {
    
    private static final QName EXTENSION_NAME = new QName(Constants.FABRIC3_NS, "jaxb-axis");
    
    private GeneratorRegistry generatorRegistry;

    public JaxbInterceptorDefinitionGenerator(@Reference GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    /**
     * Registers with the registry.
     */
    @Init
    public void init() {
        generatorRegistry.register(EXTENSION_NAME, this);
    }

    public JaxbInterceptorDefinition generate(Element policySet, GeneratorContext generatorContext) {
        return new JaxbInterceptorDefinition(null, null);
    }

}
