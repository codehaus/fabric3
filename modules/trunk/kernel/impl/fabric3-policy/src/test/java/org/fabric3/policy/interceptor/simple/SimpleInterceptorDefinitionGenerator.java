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
package org.fabric3.fabric.policy.interceptor.simple;

import javax.xml.namespace.QName;

import org.fabric3.scdl.Operation;
import org.fabric3.spi.Namespaces;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * Interceptor definition generator for simple policy set extensions.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class SimpleInterceptorDefinitionGenerator implements InterceptorDefinitionGenerator {
    
    // Qualified name of the handled element
    private static final QName EXTENSION_NAME = new QName(Namespaces.POLICY, "interceptor");
    private GeneratorRegistry generatorRegistry;

    public SimpleInterceptorDefinitionGenerator(@Reference GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    /**
     * Registers with the registry.
     */
    @Init
    public void start() {
        generatorRegistry.register(EXTENSION_NAME, this);
    }

    public SimpleInterceptorDefinition generate(Element policySetDefinition, 
                                                Operation<?> operation,
                                                LogicalBinding<?> logicalBinding) {

        String interceptorClass = policySetDefinition.getAttribute("class");

        return new SimpleInterceptorDefinition(interceptorClass);
    }

}
