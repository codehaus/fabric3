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
package org.fabric3.fabric.implementation.singleton;

import javax.xml.namespace.QName;

import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.Constants;

/**
 * @version $Rev$ $Date$
 */
public class SingletonImplementation extends Implementation<PojoComponentType> {
    public static final QName IMPLEMENTATION_SINGLETON = new QName(Constants.FABRIC3_SYSTEM_NS, "singleton");
    
    private String implementationClass;

    public SingletonImplementation() {
    }

    public SingletonImplementation(PojoComponentType componentType, String implementationClass) {
        super(componentType);
        this.implementationClass = implementationClass;
    }

    public QName getType() {
        return IMPLEMENTATION_SINGLETON;
    }

    public String getImplementationClass() {
        return implementationClass;
    }

}
