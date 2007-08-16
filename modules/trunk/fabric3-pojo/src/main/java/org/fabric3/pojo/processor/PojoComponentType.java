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

import java.util.HashMap;
import java.util.Map;

import org.fabric3.pojo.instancefactory.MemberSite;
import org.fabric3.pojo.instancefactory.Signature;
import org.fabric3.scdl.AbstractComponentType;

/**
 * A component type specialization for POJO implementations
 *
 * @version $$Rev$$ $$Date$$
 */
public class PojoComponentType extends AbstractComponentType<JavaMappedService, JavaMappedReference, JavaMappedProperty<?>> {
    private final String implClass;
    private ConstructorDefinition<?> constructorDefinition;
    private Signature initMethod;
    private Signature destroyMethod;
    private final Map<String, Resource> resources = new HashMap<String, Resource>();
    private MemberSite conversationIDMember;

    /**
     * Deprecated no-arg constructor, replaced with one that takes the POJO class.
     */
    @Deprecated
    public PojoComponentType() {
        implClass = null;
    }

    /**
     * Constructor specifying the java class name for the POJO this is describing.
     *
     * @param implClass the java class for the POJO this is describing
     */
    public PojoComponentType(String implClass) {
        this.implClass = implClass;
    }

    /**
     * Returns the java class name for the POJO this is describing.
     *
     * @return the java class name for the POJO this is describing
     */
    public String getImplClass() {
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
    public Signature getInitMethod() {
        return initMethod;
    }

    /**
     * Sets the component initializer method.
     *
     * @param initMethod the component initializer method
     */
    public void setInitMethod(Signature initMethod) {
        this.initMethod = initMethod;
    }

    /**
     * Returns the component destructor method.
     *
     * @return the component destructor method
     */
    public Signature getDestroyMethod() {
        return destroyMethod;
    }

    /**
     * Sets the component destructor method.
     *
     * @param destroyMethod the component destructor method
     */
    public void setDestroyMethod(Signature destroyMethod) {
        this.destroyMethod = destroyMethod;
    }

    public Map<String, Resource> getResources() {
        return resources;
    }

    public void add(Resource resource) {
        resources.put(resource.getName(), resource);
    }

    public MemberSite getConversationIDMember() {
        return this.conversationIDMember;
    }

    public void setConversationIDMember(MemberSite conversationIDMember) {
        this.conversationIDMember = conversationIDMember;
    }
}
