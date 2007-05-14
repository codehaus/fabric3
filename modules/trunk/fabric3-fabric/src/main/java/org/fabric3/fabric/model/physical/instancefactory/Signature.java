/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.fabric.model.physical.instancefactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * Description of a method signature.
 *
 * @version $Rev$ $Date$
 */
public class Signature {
    private String name;
    private List<String> parameterTypes;

    /**
     * Default constructor.
     */
    public Signature() {
    }

    /**
     * Constructor that initializes the signture from a name and list of parameter types.
     * @param name the method name
     * @param types the parameter types
     */
    public Signature(String name, String... types) {
        this.name = name;
        parameterTypes = Arrays.asList(types);
    }

    /**
     * Constructor that initializes the signture from a name and list of parameter types.
     * @param name the method name
     * @param types the parameter types
     */
    public Signature(String name, List<String> types) {
        this.name = name;
        parameterTypes = types;
    }

    /**
     * Constructor that initializes this signature based on the supplied method. The name is taken from the method and
     * the parameter types from the method's parameter classes.
     *
     * @param method the method to initialze from
     */
    public Signature(Method method) {
        name = method.getName();
        Class<?>[] classes = method.getParameterTypes();
        parameterTypes = new ArrayList<String>(classes.length);
        for (Class<?> paramType : classes) {
            parameterTypes.add(paramType.getName());
        }
    }

    /**
     * Return the method on the supplied class that matches this signature.
     *
     * @param clazz the class whose method should be returned
     * @return the matching method
     * @throws ClassNotFoundException if the class for one of the parameters could not be loaded
     * @throws NoSuchMethodException  if no matching method could be found
     */
    public Method getMethod(Class<?> clazz) throws ClassNotFoundException, NoSuchMethodException {
        ClassLoader cl = clazz.getClassLoader();
        Class<?>[] types = new Class<?>[parameterTypes.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = Class.forName(parameterTypes.get(i), true, cl);
        }
        while (clazz != null) {
            try {
                // TODO do we need to reject package, private, static or synthetic methods?
                return clazz.getDeclaredMethod(name, types);
            } catch (NoSuchMethodException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchMethodException(toString());
    }

    /**
     * Returns the name of the method.
     *
     * @return the name of the method
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the method.
     *
     * @param name the name of the method
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a list of class names for the method parameters.
     *
     * @return a list of class names for the method parameters
     */
    public List<String> getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Sets a list of class names for the method parameters.
     *
     * @param parameterTypes a list of class names for the method parameters
     */
    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }


    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append('(');
        if (parameterTypes.size() > 0) {
            builder.append(parameterTypes.get(0));
        }
        for (int i = 1; i < parameterTypes.size(); i++) {
            builder.append(", ").append(parameterTypes.get(i));
        }
        builder.append(')');
        return builder.toString();
    }
}
