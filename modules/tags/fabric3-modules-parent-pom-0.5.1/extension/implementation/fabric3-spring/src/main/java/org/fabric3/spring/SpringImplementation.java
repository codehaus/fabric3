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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.osoa.sca.Constants;
import org.springframework.core.io.Resource;

import org.fabric3.scdl.Implementation;

/**
 * @version
 */
public class SpringImplementation extends Implementation<SpringComponentType> {
    public static final QName IMPLEMENTATION_SPRING = new QName(Constants.SCA_NS, "implementation.spring");

    // The location attribute which points to the Spring application-context XML file
    private String location;
    // The application-context file as a Spring Resource
    private Resource resource;
    private Map<String, String> serviceNameToBeanId;

    public SpringImplementation() {
        serviceNameToBeanId = new HashMap<String, String>();
        refNameToFieldType = new HashMap<String, Class<?>>();
    }

    public QName getType() {
        return IMPLEMENTATION_SPRING;
    }

    public String getLocation() {
      return location;
    }

    public void setLocation(String location) {
    	this.location = location;
    }
    
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }
    
    public void addServiceNameToBeanId(String serviceName, String beanId) {
        serviceNameToBeanId.put(serviceName, beanId);
    }

    public String getBeanId(String serviceName) {
        return serviceNameToBeanId.get(serviceName);
    }

    private Map<String, Class<?>> refNameToFieldType;
    public void addRefNameToFieldType(String refName, Class<?> fieldType) {
        refNameToFieldType.put(refName, fieldType);
    }

    public Class<?> getFieldType(String refName) {
        return refNameToFieldType.get(refName);
    }

}
