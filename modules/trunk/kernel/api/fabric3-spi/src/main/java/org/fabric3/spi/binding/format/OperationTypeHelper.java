  /*
   * Fabric3
   * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.spi.binding.format;

import java.util.HashSet;
import java.util.Set;

import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.util.ParamTypes;

/**
 * Utility class for loading operation parameter and fault types.
 *
 * @version $Revision$ $Date$
 */
public class OperationTypeHelper {
    private OperationTypeHelper() {
    }

    /**
     * Loads input parameter types.
     *
     * @param operation the operation
     * @param loader    the classloader to use for loading types
     * @return the loaded types
     * @throws EncoderException if an error occurs loading the types
     */
    public static Set<Class<?>> loadInParameterTypes(PhysicalOperationDefinition operation, ClassLoader loader) throws EncoderException {
        Set<Class<?>> types = new HashSet<Class<?>>();
        for (String param : operation.getParameters()) {
            Class<?> clazz = loadClass(param, loader);
            types.add(clazz);
        }
        return types;
    }

    /**
     * Loads declared fault parameter types.
     *
     * @param operation the operation
     * @param loader    the classloader to use for loading types
     * @return the loaded types
     * @throws EncoderException if an error occurs loading the types
     */
    public static Set<Class<?>> loadFaultTypes(PhysicalOperationDefinition operation, ClassLoader loader) throws EncoderException {
        Set<Class<?>> types = new HashSet<Class<?>>();
        for (String param : operation.getFaultTypes()) {
            Class<?> clazz = loadClass(param, loader);
            types.add(clazz);
        }
        return types;
    }

    /**
     * Loads output parameter type.
     *
     * @param operation the operation
     * @param loader    the classloader to use for loading types
     * @return the loaded types
     * @throws EncoderException if an error occurs loading the types
     */
    public static Class<?> loadOutputType(PhysicalOperationDefinition operation, ClassLoader loader) throws EncoderException {
        // currently only one type is supported although WSDL allows multiple
        return loadClass(operation.getReturnType(), loader);
    }

    /**
     * Loads a class identified by the given name. Primitives are also handled.
     *
     * @param name   the class name
     * @param loader the classloader to use for loading
     * @return the class
     * @throws EncoderException if an error occurs loading the class
     */
    private static Class<?> loadClass(String name, ClassLoader loader) throws EncoderException {
        Class<?> clazz;
        clazz = ParamTypes.PRIMITIVES_TYPES.get(name);
        if (clazz == null) {
            try {
                clazz = loader.loadClass(name);
            } catch (ClassNotFoundException e) {
                throw new EncoderException(e);
            }
        }
        return clazz;
    }
}