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
package org.fabric3.tx.interceptor;

import javax.xml.namespace.QName;

import org.fabric3.scdl.Operation;
import org.fabric3.spi.Constants;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * Interceptor definition generator for suspend transaction policy extensions.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class TxInterceptorDefinitionGenerator implements InterceptorDefinitionGenerator {
    private static final QName EXTENSION_NAME = new QName(Constants.FABRIC3_NS, "transaction");
    private GeneratorRegistry generatorRegistry;

    public TxInterceptorDefinitionGenerator(@Reference GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    /**
     * Registers with the registry.
     */
    @Init
    public void start() {
        generatorRegistry.register(EXTENSION_NAME, this);
    }

    public PhysicalInterceptorDefinition generate(Element policyDefinition,
                                                  Operation<?> operation,
                                                  LogicalBinding<?> logicalBinding) throws GenerationException {
        String action = policyDefinition.getAttribute("action");

        return new TxInterceptorDefinition(TxAction.valueOf(action));
    }
}
