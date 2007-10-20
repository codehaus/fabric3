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
package org.fabric3.pojo.scdl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.pojo.instancefactory.Signature;

/**
 * Hold injection information for the constructor used to instantiate a component implementation instance
 *
 * @version $Rev$ $Date$
 */
public class ConstructorDefinition<T> {
    private Signature signature;
    private String declaringClass;
    // transient so the class can be deserialized without the ctor class on the classpath
    private transient Constructor<T> constructor;

    private List<String> injectionNames;

    public ConstructorDefinition(Constructor<T> constructor) {
        signature = new Signature(constructor);
        declaringClass = constructor.getDeclaringClass().getName();
        this.constructor = constructor;
        injectionNames = new ArrayList<String>();
    }

    /**
     * TODO JFM remove this method when HueristicPojoProcessor is refactored
     */
    public Constructor<T> getConstructor() {
        return constructor;
    }

    public List<String> getInjectionNames() {
        return injectionNames;
    }

    public void setInjectionNames(List<String> injectionNames) {
        this.injectionNames = injectionNames;
    }

    /**
     * Returns the list of constructor parameter types
     *
     * @return the list of constructor parameter types
     */
    public List<String> getParameterTypes() {
        return signature.getParameterTypes();
    }

    /**
     * Returns true if this definition corresponds to the given constructor
     *
     * @param ctor the constructor to match
     * @return true if this definition corresponds to the given constructor
     */
    public boolean match(Constructor ctor) {
        if (!declaringClass.equals(ctor.getDeclaringClass().getName())) {
            return false;
        }
        if (signature.getParameterTypes().size() != ctor.getParameterTypes().length) {
            return false;
        }
        for (int i = 0; i < ctor.getParameterTypes().length; i++) {
            Class<?> type = ctor.getParameterTypes()[i];
            if (!signature.getParameterTypes().get(i).equals(type.getName())) {
                return false;
            }
        }
        return true;
    }
}
