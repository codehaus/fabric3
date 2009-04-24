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
package org.fabric3.hessian.generator;

import org.osoa.sca.annotations.EagerInit;
import org.w3c.dom.Element;

import org.fabric3.hessian.provision.Encoding;
import org.fabric3.hessian.provision.HessianReferenceInterceptorDefinition;
import org.fabric3.hessian.provision.HessianServiceInterceptorDefinition;
import org.fabric3.model.type.component.Encodings;
import org.fabric3.model.type.service.Operation;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;

/**
 * Generates interceptor definitions for operations marked with the Hessian intent.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class HessianInterceptorDefinitionGenerator implements InterceptorDefinitionGenerator {

    public PhysicalInterceptorDefinition generate(Element policySet, Operation<?> operation, LogicalBinding<?> logicalBinding)
            throws GenerationException {
        String encoding = logicalBinding.getDefinition().getEncoding();
        if (Encodings.JAVA.equals(encoding)) {
            // The binding does not use an encoding scheme so ignore.
            return null;
        }

        Encoding type;
        if (Encodings.ASCII.equals(encoding)) {
            type = Encoding.ASCII;
        } else if (Encodings.BINARY.equals(encoding)) {
            type = Encoding.BINARY;
        } else {
            throw new GenerationException("Unsupported encoding: " + encoding);
        }
        if (logicalBinding.getParent() instanceof LogicalService) {
            if (logicalBinding.isCallback()) {
                // callbacks on the service side of a wire take a reference interceptor since the callback invocation originates there
                return new HessianReferenceInterceptorDefinition(type);

            } else {
                return new HessianServiceInterceptorDefinition(type);
            }
        } else {
            if (logicalBinding.isCallback()) {
                // callbacks on the reference side of a wire take a service interceptor since the callback is received there
                return new HessianServiceInterceptorDefinition(type);
            } else {
                return new HessianReferenceInterceptorDefinition(type);
            }
        }
    }

}