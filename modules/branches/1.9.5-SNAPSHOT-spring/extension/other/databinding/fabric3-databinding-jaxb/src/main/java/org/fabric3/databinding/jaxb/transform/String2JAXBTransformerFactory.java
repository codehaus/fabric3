/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.databinding.jaxb.transform;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.databinding.jaxb.factory.JAXBContextFactory;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.xsd.XSDType;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerFactory;

/**
 * Creates Transformers capable of marshalling serialized XML Strings to JAXB types.
 *
 * @version $Rev: 7720 $ $Date: 2009-09-30 10:28:56 +0200 (Wed, 30 Sep 2009) $
 */
public class String2JAXBTransformerFactory implements TransformerFactory {
    private JAXBContextFactory contextFactory;

    public String2JAXBTransformerFactory(@Reference JAXBContextFactory contextFactory) {
        this.contextFactory = contextFactory;
    }

    public boolean canTransform(DataType<?> source, DataType<?> target) {
        return source instanceof XSDType && String.class.equals(source.getPhysical()) && target instanceof JavaType;
    }

    public Transformer<?, ?> create(DataType<?> source, DataType<?> target, List<Class<?>> sourceTypes, List<Class<?>> targetTypes)
            throws TransformationException {
        try {
            Set<Class<?>> types = new HashSet<Class<?>>(sourceTypes);
            types.addAll(targetTypes);
            JAXBContext jaxbContext = contextFactory.createJAXBContext(types.toArray(new Class<?>[types.size()]));
            if (sourceTypes.size() == 1) {
                Class<?> type = targetTypes.iterator().next();
                return createTransformer(type, jaxbContext);
            } else if (sourceTypes.size() > 1) {
                // the conversion must handle multiple parameters, which will be passed to the transformer in an array
                Transformer<?, ?>[] transformers = new Transformer<?, ?>[sourceTypes.size()];
                for (int i = 0; i < sourceTypes.size(); i++) {
                    Class<?> type = sourceTypes.get(i);
                    transformers[i] = createTransformer(type, jaxbContext);
                }
                return new MultiValueArrayTransformer(transformers);
            } else {
                throw new UnsupportedOperationException("Null parameter operations not yet supported");
            }
        } catch (JAXBException e) {
            throw new TransformationException(e);
        }
    }

    private Transformer<String, Object> createTransformer(Class<?> type, JAXBContext jaxbContext) {
        if (type.isAnnotationPresent(XmlRootElement.class)) {
            return new String2JAXBObjectTransformer(jaxbContext);
        } else {
            return new String2JAXBElementTransformer(jaxbContext, type);
        }
    }


}