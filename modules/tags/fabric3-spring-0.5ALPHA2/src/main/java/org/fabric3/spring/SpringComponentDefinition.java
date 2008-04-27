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
package org.fabric3.spring;

import java.util.Map;

import org.fabric3.pojo.implementation.PojoComponentDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.springframework.core.io.Resource;

/**
 * Represents the physical component definition for a Spring implementation.
 *
 * @version $Rev$ $Date$
 */
public class SpringComponentDefinition extends PojoComponentDefinition {
    private Resource resource;
    private String springBeanId;
    private Map<String, ReferenceDefinition> references;
    
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    public String getSpringBeanId() {
        return springBeanId;
    }

    public void setSpringBeanId(String springBeanId) {
        this.springBeanId = springBeanId;
    }

    public Map<String, ReferenceDefinition> getReferences() {
        return references;
    }

    public void setReferences(Map<String, ReferenceDefinition> references) {
        this.references = references;
    }

}
