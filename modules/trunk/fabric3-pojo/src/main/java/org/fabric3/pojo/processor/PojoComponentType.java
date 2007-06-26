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
package org.fabric3.pojo.processor;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.model.type.ComponentType;

/**
 * A component type specialization for POJO implementations
 *
 * @version $$Rev$$ $$Date$$
 */
public class PojoComponentType extends ComponentType<JavaMappedService, JavaMappedReference, JavaMappedProperty<?>> {
    private final Class<?> implClass;
    private ConstructorDefinition<?> constructorDefinition;
    private Method initMethod;
    private Method destroyMethod;
    private final Map<String, Resource> resources = new HashMap<String, Resource>();
    private Member conversationIDMember;

    /**
     * Deprecated no-arg constructor, replaced with one that takes the POJO class.
     */
    @Deprecated
    public PojoComponentType() {
        implClass = null;
    }

    /**
     * Constructor specifying the java class for the POJO this is describing.
     *
     * @param implClass the java class for the POJO this is describing
     */
    public PojoComponentType(Class<?> implClass) {
        this.implClass = implClass;
    }

    /**
     * Returns the java class for the POJO this is describing.
     *
     * @return the java class for the POJO this is describing
     */
    public Class<?> getImplClass() {
        return implClass;
    }

    /**
     * Returns the constructor used to instantiate implementation instances.
     *
     * @return the constructor used to instantiate implementation instances
     */
    public ConstructorDefinition<?> getConstructorDefinition() {
        return constructorDefinition;
    }

    /**
     * Sets the constructor used to instantiate implementation instances
     *
     * @param definition the constructor used to instantiate implementation instances
     */
    public void setConstructorDefinition(ConstructorDefinition<?> definition) {
        this.constructorDefinition = definition;
    }

    /**
     * Returns the component initializer method.
     *
     * @return the component initializer method
     */
    public Method getInitMethod() {
        return initMethod;
    }

    /**
     * Sets the component initializer method.
     *
     * @param initMethod the component initializer method
     */
    public void setInitMethod(Method initMethod) {
        this.initMethod = initMethod;
    }

    /**
     * Returns the component destructor method.
     *
     * @return the component destructor method
     */
    public Method getDestroyMethod() {
        return destroyMethod;
    }

    /**
     * Sets the component destructor method.
     *
     * @param destroyMethod the component destructor method
     */
    public void setDestroyMethod(Method destroyMethod) {
        this.destroyMethod = destroyMethod;
    }

    public Map<String, Resource> getResources() {
        return resources;
    }

    public void add(Resource resource) {
        resources.put(resource.getName(), resource);
    }

    public Member getConversationIDMember() {
        return this.conversationIDMember;
    }

    public void setConversationIDMember(Member conversationIDMember) {
        this.conversationIDMember = conversationIDMember;
    }
}
