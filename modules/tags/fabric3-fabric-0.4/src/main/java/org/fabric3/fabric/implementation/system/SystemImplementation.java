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
package org.fabric3.fabric.implementation.system;

import javax.xml.namespace.QName;

import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.Constants;

/**
 * Represents the system composite implementation
 *
 * @version $Rev$ $Date$
 */
public class SystemImplementation extends Implementation<PojoComponentType> {
    public static final QName IMPLEMENTATION_SYSTEM = new QName(Constants.FABRIC3_SYSTEM_NS, "implementation.system");
    private String implementationClass;

    public SystemImplementation() {
    }

    public QName getType() {
        return IMPLEMENTATION_SYSTEM;
    }

    public SystemImplementation(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    public String getImplementationClass() {
        return implementationClass;
    }

    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }
}
