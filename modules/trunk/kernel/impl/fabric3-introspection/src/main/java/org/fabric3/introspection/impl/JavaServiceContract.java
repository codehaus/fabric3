/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.introspection.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.scdl.ServiceContract;

/**
 * Represents a service contract specified using a Java interface
 *
 * @version $Rev$ $Date$
 */
public class JavaServiceContract extends ServiceContract<Type> {
    // NOTE: this class cannot reference the actual Java class it represents as #isAssignableFrom may be performed
    // accross classloaders. This class may also be deserialized as part of a domain assembly in a context where the
    // Java class may not be present on the classpath.
    private String interfaceClass;
    private List<String> interfaces;
    private String superType;
    private List<MethodSignature> methodSignatures;

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
    public boolean isAssignableFrom(ServiceContract<?> contract) {
        if (JavaServiceContract.class.isInstance(contract)) {
            return isJavaAssignableFrom(JavaServiceContract.class.cast(contract));
        }
        // TODO handle the case where the contract is defined using a different IDL
        return false;
    }

    private boolean isJavaAssignableFrom(JavaServiceContract contract) {
        if ((superType == null && contract.superType != null)
                || (superType != null && !superType.equals(contract.superType))) {
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

    private class MethodSignature {
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
