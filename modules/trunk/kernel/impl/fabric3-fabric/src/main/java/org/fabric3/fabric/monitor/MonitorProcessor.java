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
package org.fabric3.fabric.monitor;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.pojo.processor.DuplicateResourceException;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.InvalidSetterException;
import static org.fabric3.pojo.processor.JavaIntrospectionHelper.toPropertyName;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.scdl.MemberSite;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.InvalidServiceContractException;

/**
 * Processes an {@link @Monitor} annotation}
 *
 * @version $Rev$ $Date$
 */
public class MonitorProcessor extends ImplementationProcessorExtension {
    private final ContractProcessor contractProcessor;

    public MonitorProcessor(@Reference(name="processorRegistry")ContractProcessor contractProcessor) {
        this.contractProcessor = contractProcessor;
    }

    public void visitMethod(Method method, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
        Monitor annotation = method.getAnnotation(Monitor.class);
        if (annotation == null) {
            return;
        }
        if (method.getParameterTypes().length != 1) {
            throw new InvalidSetterException(method);
        }

        Class<?> resourceType = method.getParameterTypes()[0];

        String name = toPropertyName(method.getName());
        if (type.getResources().get(name) != null) {
            throw new DuplicateResourceException(name);
        }

        MonitorResource resource = createResource(name, resourceType, method);
        type.add(resource);
    }

    public void visitField(Field field, PojoComponentType type, IntrospectionContext context) throws ProcessingException {
        Monitor annotation = field.getAnnotation(Monitor.class);
        if (annotation == null) {
            return;
        }

        Class<?> resourceType = field.getType();
        String name = field.getName();
        if (type.getResources().get(name) != null) {
            throw new DuplicateResourceException(name);
        }

        MonitorResource resource = createResource(name, resourceType, field);
        type.add(resource);
    }

    private MonitorResource createResource(String name, Class<?> type, Member member) throws ProcessingException {
        try {
            ServiceContract<?> serviceContract = contractProcessor.introspect(type);
            MemberSite site = new MemberSite(member);
            return new MonitorResource(name, site, false, serviceContract);
        } catch (InvalidServiceContractException e) {
            throw new ProcessingException(e);
        }
    }
}
