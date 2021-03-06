/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.databinding.json.transform;

import java.util.List;

import org.codehaus.jackson.jaxrs.Annotations;
import org.codehaus.jackson.jaxrs.MapperConfigurator;
import org.codehaus.jackson.map.ObjectMapper;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.json.JsonType;
import org.fabric3.spi.transform.TransformerFactory;

/**
 * Creates Transformers capable of marshalling serialized XML Strings to Java types using JSON.
 *
 * @version $Rev: 7720 $ $Date: 2009-09-30 10:28:56 +0200 (Wed, 30 Sep 2009) $
 */
public class Object2StringJsonTransformerFactory implements TransformerFactory {
    private final static Annotations[] DEFAULT_ANNOTATIONS = {Annotations.JACKSON, Annotations.JAXB};
    private MapperConfigurator configurator;

    public Object2StringJsonTransformerFactory() {
        configurator = new MapperConfigurator(null, DEFAULT_ANNOTATIONS);
    }

    public boolean canTransform(DataType<?> source, DataType<?> target) {
        return target instanceof JsonType && String.class.equals(target.getPhysical()) && source instanceof JavaType;
    }

    public Object2StringJsonTransformer create(DataType<?> source, DataType<?> target, List<Class<?>> sourceTypes, List<Class<?>> targetTypes) {
        ObjectMapper mapper = configurator.getDefaultMapper();
        return new Object2StringJsonTransformer(mapper);
    }


}