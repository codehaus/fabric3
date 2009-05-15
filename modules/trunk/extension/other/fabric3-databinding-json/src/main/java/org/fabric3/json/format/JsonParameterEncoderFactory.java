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
package org.fabric3.json.format;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.OperationTypeHelper;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.binding.format.ParameterEncoderFactory;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Creates JsonParameterEncoder instances.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JsonParameterEncoderFactory implements ParameterEncoderFactory {

    public ParameterEncoder getInstance(Wire wire, ClassLoader loader) throws EncoderException {
        Map<String, OperationTypes> mappings = new HashMap<String, OperationTypes>();
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition definition = chain.getPhysicalOperation();
            String name = definition.getName();
            Set<Class<?>> inParams = OperationTypeHelper.loadInParameterTypes(definition, loader);
            if (inParams.size() > 1) {
                throw new EncoderException("Multiple parameters not supported");
            }

            Class<?> inParam;
            if (inParams.isEmpty()) {
                inParam = null;
            } else {
                inParam = inParams.iterator().next();
            }
            Class<?> outParam = OperationTypeHelper.loadOutputType(definition, loader);
            Set<Class<?>> faults = OperationTypeHelper.loadFaultTypes(definition, loader);
            OperationTypes types = new OperationTypes(inParam, outParam, faults);
            mappings.put(name, types);
        }
        return new JsonParameterEncoder(mappings);
    }

}