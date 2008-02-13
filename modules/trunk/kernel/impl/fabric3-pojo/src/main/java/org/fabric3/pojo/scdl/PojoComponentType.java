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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.MemberSite;
import org.fabric3.scdl.Signature;

/**
 * A component type specialization for POJO implementations
 *
 * @version $$Rev$$ $$Date$$
 */
public class PojoComponentType extends AbstractComponentType<JavaMappedService,
        JavaMappedReference,
        JavaMappedProperty<?>,
        JavaMappedResource> {
    private String implClass;
    private ConstructorDefinition<?> constructorDefinition;
    private Signature initMethod;
    private Signature destroyMethod;
    private MemberSite conversationIDMember;
    private MemberSite componentContextMember;
    private MemberSite requestContextMember;
    private List<MemberSite> callbackMembers = new ArrayList<MemberSite>();

    /**
     * Constructor used only for deserialization
     */
    public PojoComponentType() {
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
     * Returns a collection of defined callback injection sites for the component implementation
     *
     * @return the callback injection sites
     */
    public List<MemberSite> getCallbackSites() {
        return Collections.unmodifiableList(callbackMembers);
    }

    /**
     * Adds a defined callback injection site for the component implementation
     *
     * @param site the field or setter method the callback is mapped to
     */
    public void addCallbackSite(MemberSite site) {
        callbackMembers.add(site);
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

    public MemberSite getConversationIDMember() {
        return this.conversationIDMember;
    }

    public void setConversationIDMember(MemberSite conversationIDMember) {
        this.conversationIDMember = conversationIDMember;
    }

    public MemberSite getComponentContextMember() {
        return componentContextMember;
    }

    public void setComponentContextMember(MemberSite componentContextMember) {
        this.componentContextMember = componentContextMember;
    }

    public MemberSite getRequestContextMember() {
        return requestContextMember;
    }

    public void setRequestContextMember(MemberSite requestContextMember) {
        this.requestContextMember = requestContextMember;
    }
}
