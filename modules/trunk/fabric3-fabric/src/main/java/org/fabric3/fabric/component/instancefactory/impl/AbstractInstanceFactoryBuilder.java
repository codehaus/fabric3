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
package org.fabric3.fabric.component.instancefactory.impl;

import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilder;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;

import org.osoa.sca.annotations.Reference;

/**
 * Abstarct implementation that supportes registration for InstanceFactoryProviders.
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractInstanceFactoryBuilder<IFP extends InstanceFactoryProvider,
        IFPD extends InstanceFactoryDefinition> implements InstanceFactoryBuilder<IFP, IFPD> {

    /**
     * Returns the InstanceFactoryDefinition the implementation handles.
     *
     * @return the InstanceFactoryDefinition the implementation handles.
     */
    protected abstract Class<?> getIfpdClass();

    /**
     * Injects the builder registry.
     *
     * @param registry The builder registry.
     */
    @Reference
    public void setBuilderRegistry(InstanceFactoryBuilderRegistry registry) {
        registry.register(getIfpdClass(), this);
    }

}
