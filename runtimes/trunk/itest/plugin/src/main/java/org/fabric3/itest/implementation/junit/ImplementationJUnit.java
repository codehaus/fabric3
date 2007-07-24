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
package org.fabric3.itest.implementation.junit;

import org.fabric3.pojo.processor.PojoComponentType;
import org.fabric3.scdl.Implementation;

/**
 * @version $Rev$ $Date$
 */
public class ImplementationJUnit extends Implementation<PojoComponentType> {
    private String className;

    /**
     * Constructor supplying the name of the JUnit test class
     *
     * @param className the name of the JUnit test class
     */
    public ImplementationJUnit(String className) {
        this.className = className;
    }

    /**
     * Returns the name of the JUnit test class.
     *
     * @return the name of the JUnit test class
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the name of the JUnit test class.
     *
     * @param className the name of the JUnit test class
     */
    public void setClassName(String className) {
        this.className = className;
    }
}
