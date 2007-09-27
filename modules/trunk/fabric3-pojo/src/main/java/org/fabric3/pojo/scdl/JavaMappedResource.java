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

import java.lang.reflect.Member;

import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.spi.ObjectFactory;

/**
 * A resource dependency declared by a Java component implementation
 * 
 * @version $Rev$ $Date$
 * @param <T> the Java type of the resource
 */
public class JavaMappedResource<T> extends ResourceDefinition {

    private Member member;
    private Class<T> type;
    private ObjectFactory<T> objectFactory;

    public JavaMappedResource(String name, Class<T> type, Member member) {
        super(name);
        this.type = type;
        this.member = member;
    }

    /**
     * Returns the Member that this resource is mapped to.
     * 
     * @return the Member that this resource is mapped to
     */
    public Member getMember() {
        return member;
    }

    /**
     * Returns the resource type
     * 
     * @return the resource type
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Returns the obeject factory
     * 
     * @return the object factory
     */
    public ObjectFactory<T> getObjectFactory() {
        return objectFactory;
    }

    /**
     * Sets the object factory
     */
    public void setObjectFactory(ObjectFactory<T> objectFactory) {
        this.objectFactory = objectFactory;
    }
    
}
