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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.model.type.service;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a service contract specified using a Java interface
 *
 * @version $Rev$ $Date$
 */
public class JavaServiceContract extends ServiceContract {

    private static final long serialVersionUID = -7360275776965712638L;
    // NOTE: this class cannot reference the actual Java class it represents as #isAssignableFrom may be performed
    // accross classloaders. This class may also be deserialized as part of a domain assembly in a context where the
    // Java class may not be present on the classpath.
    private String interfaceClass;
    private List<String> interfaces;
    private String superType;
    private List<MethodSignature> methodSignatures;

    //http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6176992
    private static final Map<Class, Class> PRIMITIVE_TYPES = new HashMap<Class, Class>();

    static {
        PRIMITIVE_TYPES.put(byte.class, Byte.class);
        PRIMITIVE_TYPES.put(short.class, Short.class);
        PRIMITIVE_TYPES.put(char.class, Character.class);
        PRIMITIVE_TYPES.put(int.class, Integer.class);
        PRIMITIVE_TYPES.put(long.class, Long.class);
        PRIMITIVE_TYPES.put(float.class, Float.class);
        PRIMITIVE_TYPES.put(double.class, Double.class);
        PRIMITIVE_TYPES.put(boolean.class, Boolean.class);
        PRIMITIVE_TYPES.put(void.class, Void.class);
    }

    public JavaServiceContract(Class<?> interfaceClazz) {
        methodSignatures = new ArrayList<MethodSignature>();
        Class<?> superClass = interfaceClazz.getSuperclass();
        if (superClass != null) {
            superType = superClass.getName();
        }
        interfaces = new ArrayList<String>();
        for (Method method : interfaceClazz.getDeclaredMethods()) {
            MethodSignature signature = new MethodSignature(method);
            if (!methodSignatures.contains(signature)) {
                methodSignatures.add(signature);
            }
        }
        this.interfaceClass = interfaceClazz.getName();
        addInterfaces(interfaceClazz, interfaces);
    }

    public String getQualifiedInterfaceName() {
        return getInterfaceClass();
    }

    /**
     * Returns the fully qualified class name used to represent the service contract.
     *
     * @return the class name used to represent the service contract
     */
    public String getInterfaceClass() {
        return interfaceClass;
    }

    /*
     * Determines if the class or interface represented by this
     * <code>Class</code> object is either the same as, or is a superclass or
     * superinterface of, the class or interface represented by the specified
     * <code>Class</code> parameter. It returns <code>true</code> if so;
    
     */
    public boolean isAssignableFrom(ServiceContract contract) {
        if (JavaServiceContract.class.isInstance(contract)) {
            return isJavaAssignableFrom(JavaServiceContract.class.cast(contract));
        } else {
            return isNonJavaAssignableFrom(contract);
        }
    }

    private boolean isJavaAssignableFrom(JavaServiceContract contract) {
        if ((superType == null && contract.superType != null) || (superType != null && !superType.equals(contract.superType))) {
            return false;
        }
        if (interfaceClass.equals(contract.interfaceClass)) {
            for (MethodSignature signature : methodSignatures) {
                if (!contract.methodSignatures.contains(signature)) {
                    return false;
                }
            }
            return true;
        } else {
            // check the interfaces 
            for (String superType : contract.interfaces) {
                if (superType.equals(interfaceClass)) {
                    // need to match params as well
                    return true;
                }
            }
        }
        return false;

    }

    private boolean isNonJavaAssignableFrom(ServiceContract contract) {
        //compare contract operations
        List<Operation> theirOperations = contract.getOperations();
        Map<String, Operation> theirOperationNames = new HashMap<String, Operation>();
        for (Operation o : theirOperations) {
            theirOperationNames.put(o.getName(), o);
        }
        List<Operation> myOperations = this.getOperations();
        for (Operation o : myOperations) {
            Operation theirs = theirOperationNames.remove(o.getName());
            if (theirs == null) {
                return false;
            }
            List<DataType<?>> myParams = o.getInputTypes();
            List<DataType<?>> theirParams = theirs.getInputTypes();

            if (myParams.size() == theirParams.size()) {
                for (int i = 0; i < myParams.size(); i++) {
                    if (!compareTypes(myParams.get(i), theirParams.get(i))) {
                        return false;
                    }
                }
            } else {
                return false;
            }

            if (!compareTypes(o.getOutputType(), theirs.getOutputType())) {
                return false;
            }

            List<DataType<?>> theirFaults = theirs.getFaultTypes();
            List<DataType<?>> faults = o.getFaultTypes();
            for (DataType theirFault : theirFaults) {
                boolean matches = false;
                for (DataType myFault : faults) {
                    if (compareTypes(theirFault, myFault)) {
                        matches = true;
                        break;
                    }
                }
                if (!matches) {
                    return false;
                }
            }
        }
        return true;


    }

    private boolean compareTypes(DataType mine, DataType theirs) {
        Class<?> myClass = mine.getPhysical();
        Class<?> theirClass = theirs.getPhysical();
        if (myClass.isPrimitive()) {
            myClass = PRIMITIVE_TYPES.get(myClass);
        }
        if (theirClass.isPrimitive()) {
            theirClass = PRIMITIVE_TYPES.get(theirClass);
        }
        return theirClass.isAssignableFrom(myClass);

    }

    /**
     * Adds all interfaces implemented/extended by the class, including those of its ancestors.
     *
     * @param interfaze  the class to introspect
     * @param interfaces the collection of interfaces to add to
     */
    private void addInterfaces(Class<?> interfaze, List<String> interfaces) {
        for (Class<?> superInterface : interfaze.getInterfaces()) {
            if (!interfaces.contains(superInterface.getName())) {
                interfaces.add(superInterface.getName());
                addInterfaces(superInterface, interfaces);
            }
        }
    }

    private class MethodSignature implements Serializable {

        private static final long serialVersionUID = 8945587852354777957L;
        String name;
        List<String> parameters;
        String returnType;

        public MethodSignature(Method method) {
            name = method.getName();
            returnType = method.getReturnType().getName();
            parameters = new ArrayList<String>();
            for (Class<?> param : method.getParameterTypes()) {
                parameters.add(param.getName());
            }
        }

        public boolean equals(Object object) {
            if (!(object instanceof MethodSignature)) {
                return false;
            }
            MethodSignature other = (MethodSignature) object;
            if (!name.equals(other.name)) {
                return false;
            }
            if (!returnType.equals(other.returnType)) {
                return false;
            }
            if (parameters.size() != other.parameters.size()) {
                return false;
            }
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < parameters.size(); i++) {
                if (!parameters.get(i).equals(other.parameters.get(i))) {
                    return false;
                }
            }
            return true;
        }
    }
}
