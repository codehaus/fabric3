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
package org.fabric3.jaxb.format;

import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.OperationTypeHelper;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.binding.format.ParameterEncoderFactory;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Creates JAXBParameterEncoder instances.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JAXBParameterEncoderFactory implements ParameterEncoderFactory {

    public ParameterEncoder getInstance(Wire wire, ClassLoader loader) throws EncoderException {

        Set<Class<?>> types = new HashSet<Class<?>>();
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition definition = chain.getPhysicalOperation();
            Set<Class<?>> inParams = OperationTypeHelper.loadInParameterTypes(definition, loader);
            types.addAll(inParams);
            Class<?> outParam = OperationTypeHelper.loadOutputType(definition, loader);
            types.add(outParam);
            Set<Class<?>> faults = OperationTypeHelper.loadFaultTypes(definition, loader);
            types.addAll(faults);
        }

        try {
            JAXBContext jaxbContext = createJAXBContext(types);
            return new JAXBParameterEncoder(jaxbContext);
        } catch (JAXBException e) {
            throw new EncoderException(e);
        }
    }

    /**
     * Constructs a JAXB context by introspecting a set of classnames.
     *
     * @param types the context class types
     * @return a JAXB context
     * @throws JAXBException if an error occurs creating the JAXB context
     */
    private JAXBContext createJAXBContext(Set<Class<?>> types) throws JAXBException {
        Class<?>[] classes = types.toArray(new Class<?>[types.size()]);
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader cl = getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(cl);
            return JAXBContext.newInstance(classes);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

}