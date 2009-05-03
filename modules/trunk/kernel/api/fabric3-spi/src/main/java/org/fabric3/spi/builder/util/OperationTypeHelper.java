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
package org.fabric3.spi.builder.util;

import java.util.HashSet;
import java.util.Set;

import org.fabric3.spi.builder.WiringException;
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
     * @throws WiringException if an error occurs loading the types
     */
    public static Set<Class<?>> loadInParameterTypes(PhysicalOperationDefinition operation, ClassLoader loader) throws WiringException {
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
     * @throws WiringException if an error occurs loading the types
     */
    public static Set<Class<?>> loadFaultTypes(PhysicalOperationDefinition operation, ClassLoader loader) throws WiringException {
        Set<Class<?>> types = new HashSet<Class<?>>();
        for (String param : operation.getFaultTypes()) {
            Class<?> clazz = loadClass(param, loader);
            types.add(clazz);
        }
        return types;
    }

    /**
     * Loads output parameter types.
     *
     * @param operation the operation
     * @param loader    the classloader to use for loading types
     * @return the loaded types
     * @throws WiringException if an error occurs loading the types
     */
    public static Set<Class<?>> loadOutputTypes(PhysicalOperationDefinition operation, ClassLoader loader) throws WiringException {
        Set<Class<?>> types = new HashSet<Class<?>>();
        // currently only one type is supported although WSDL allows multiple
        Class<?> clazz = loadClass(operation.getReturnType(), loader);
        types.add(clazz);
        return types;
    }

    /**
     * Loads a class identified by the given name. Primitives are also handled.
     *
     * @param name   the class name
     * @param loader the classloader to use for loading
     * @return the class
     * @throws WiringException if an error occurs loading the class
     */
    private static Class<?> loadClass(String name, ClassLoader loader) throws WiringException {
        Class<?> clazz;
        clazz = ParamTypes.PRIMITIVES_TYPES.get(name);
        if (clazz == null) {
            try {
                clazz = loader.loadClass(name);
            } catch (ClassNotFoundException e) {
                throw new WiringException(e);
            }
        }
        return clazz;
    }
}