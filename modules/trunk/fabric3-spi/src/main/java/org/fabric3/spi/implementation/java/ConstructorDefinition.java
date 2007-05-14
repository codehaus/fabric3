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
package org.fabric3.spi.implementation.java;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Hold injection information for the constructor used to instantiate a component implementation instance
 *
 * @version $Rev$ $Date$
 */
public class ConstructorDefinition<T> {

    private Constructor<T> constructor;
    private List<String> injectionNames;

    public ConstructorDefinition(Constructor<T> constructor) {
        this.constructor = constructor;
        injectionNames = new ArrayList<String>();
    }

    public Constructor<T> getConstructor() {
        return constructor;
    }

    public List<String> getInjectionNames() {
        return injectionNames;
    }

    public void setInjectionNames(List<String> injectionNames) {
        this.injectionNames = injectionNames;
    }
}
