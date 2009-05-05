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
package org.fabric3.jaxb.serializer;

import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.binding.serializer.SerializationException;
import org.fabric3.spi.binding.serializer.Serializer;
import org.fabric3.spi.binding.serializer.SerializerFactory;

/**
 * Creates JAXBSerializers.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JAXBSerializerFactory implements SerializerFactory {

    public Serializer getInstance(Set<Class<?>> types, Set<Class<?>> faultTypes) throws SerializationException {
        try {
            JAXBContext jaxbContext = getJAXBContext(types, faultTypes);
            return new JAXBSerializer(jaxbContext);
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    /**
     * Constructs a JAXB context by introspecting a set of classnames.
     *
     * @param types      the context class names
     * @param faultTypes the fault types
     * @return a JAXB context
     * @throws JAXBException if an error occurs creating the JAXB context
     */
    private JAXBContext getJAXBContext(Set<Class<?>> types, Set<Class<?>> faultTypes) throws JAXBException {
        Class<?>[] classes = new Class<?>[types.size() + faultTypes.size()];
        int i = 0;
        for (Class<?> type : types) {
            classes[i] = type;
            ++i;
        }
        for (Class<?> faultType : faultTypes) {
            classes[i] = faultType;
            ++i;
        }
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
