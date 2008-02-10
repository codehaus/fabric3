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
package org.fabric3.scdl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description of a method signature.
 *
 * @version $Rev$ $Date$
 */
public class Signature {
    private static final Map<String, Class<?>> PRIMITIVES_TYPES;

    static {
        PRIMITIVES_TYPES = new HashMap<String, Class<?>>();
        PRIMITIVES_TYPES.put("boolean", Boolean.TYPE);
        PRIMITIVES_TYPES.put("byte", Byte.TYPE);
        PRIMITIVES_TYPES.put("short", Short.TYPE);
        PRIMITIVES_TYPES.put("int", Integer.TYPE);
        PRIMITIVES_TYPES.put("long", Long.TYPE);
        PRIMITIVES_TYPES.put("float", Float.TYPE);
        PRIMITIVES_TYPES.put("double", Double.TYPE);
    }

    private String name;
    private List<String> parameterTypes;
    private boolean isConstructor = false;

    /**
     * Default constructor.
     */
    public Signature() {
    }

    /**
     * Constructor that initializes the signture from a name and list of parameter types.
     *
     * @param name  the method name
     * @param types the parameter types
     */
    public Signature(String name, String... types) {
        this.name = name;
        parameterTypes = Arrays.asList(types);
    }

    /**
     * Constructor that initializes the signture from a name and list of parameter types.
     *
     * @param name  the method name
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
        setParameterTypes(method.getParameterTypes());
    }

    public Signature(Constructor constructor) {
        name = "<init>";
        setParameterTypes(constructor.getParameterTypes());
        isConstructor = true;
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
        if (isConstructor) throw new AssertionError("Illegal call to getMethod on a Constructor Signature");
        Class<?>[] types = getParameterTypes(clazz.getClassLoader());
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
     * Return the constructor on the supplied class that matches this signature.
     *
     * @param clazz the class whose constructor should be returned
     * @return the matching constructor
     * @throws ClassNotFoundException if the class for one of the parameters could not be loaded
     * @throws NoSuchMethodException  if no matching constructor could be found
     */
    public Constructor getConstructor(Class<?> clazz) throws ClassNotFoundException, NoSuchMethodException {
        if (!isConstructor) throw new AssertionError("Illegal call to getConstructor on a Method Signature");
        Class<?>[] types = getParameterTypes(clazz.getClassLoader());
        return clazz.getConstructor(types);
    }

    private void setParameterTypes(Class<?>[] classes) {
        parameterTypes = new ArrayList<String>(classes.length);
        for (Class<?> paramType : classes) {
            parameterTypes.add(paramType.getName());
        }
    }

    private Class<?>[] getParameterTypes(ClassLoader cl) throws ClassNotFoundException {
        Class<?>[] types = new Class<?>[parameterTypes.size()];
        for (int i = 0; i < types.length; i++) {
            String type = parameterTypes.get(i);
            Class clazz = PRIMITIVES_TYPES.get(type);
            types[i] = (clazz != null) ? clazz : Class.forName(type, true, cl);
        }
        return types;
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

    public boolean isConstructor() {
        return isConstructor;
    }

    public void setConstructor(boolean constructor) {
        isConstructor = constructor;
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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Signature signature = (Signature) o;

        return name.equals(signature.name) && parameterTypes.equals(signature.parameterTypes);

    }

    public int hashCode() {
        return name.hashCode() + 31 * parameterTypes.hashCode();
    }
}
