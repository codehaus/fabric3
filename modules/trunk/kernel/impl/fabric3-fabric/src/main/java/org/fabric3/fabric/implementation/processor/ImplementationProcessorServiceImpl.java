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
package org.fabric3.fabric.implementation.processor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.List;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Resource;
import org.fabric3.api.annotation.Monitor;
import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.InvalidServiceContractException;
import org.fabric3.pojo.processor.ImplementationProcessorService;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ValueSource;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.fabric.monitor.MonitorResource;

/**
 * The default implementation of an <code>ImplementationProcessorService</code>
 *
 * @version $Rev$ $Date$
 */
public class ImplementationProcessorServiceImpl implements ImplementationProcessorService {
    private final IntrospectionHelper helper;
    private final ContractProcessor contractProcessor;

    public ImplementationProcessorServiceImpl(@Reference ContractProcessor contractProcessor,
                                              @Reference IntrospectionHelper helper) {
        this.contractProcessor = contractProcessor;
        this.helper = helper;
    }

    public ServiceDefinition createService(Class<?> interfaze) throws InvalidServiceContractException {
        ServiceContract<?> contract = contractProcessor.introspect(interfaze);
        return new ServiceDefinition(interfaze.getSimpleName(), contract);
    }

    public boolean areUnique(Class[] collection) {
        return collection.length == 0 || areUnique(collection, 0);
    }

    public void addName(List<String> names, int pos, String name) {
        if (names.size() < pos) {
            for (int i = 0; i < pos; i++) {
                names.add(i, "");
            }
            names.add(name);
        } else if (names.size() > pos) {
            names.remove(pos);
            names.add(pos, name);
        } else {
            names.add(pos, name);
        }
    }

    public void processParameters(Constructor<?> constructor, PojoComponentType componentType) throws ProcessingException {
        try {
            Type[] parameterTypes = constructor.getGenericParameterTypes();
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            param: for (int i = 0; i < parameterTypes.length; i++) {
                Annotation[] annotations = parameterAnnotations[i];
                for (Annotation annotation : annotations) {
                    Class<? extends Annotation> annotationType = annotation.annotationType();
                    if (Property.class.equals(annotationType)) {
                        processProperty((Property) annotation, constructor, i, componentType);
                        continue param;
                    } else if (Reference.class.equals(annotationType)) {
                        processReference((Reference) annotation, constructor, i, componentType);
                        continue param;
                    } else if (Monitor.class.equals(annotationType)) {
                        processMonitor(constructor, i, componentType);
                        continue param;
                    }
                }
                ValueSource.ValueSourceType sourceType = helper.inferType(parameterTypes[i]);
                switch (sourceType) {
                case PROPERTY:
                    processProperty(null, constructor, i, componentType);
                    break;
                case REFERENCE:
                    processReference(null, constructor, i, componentType);
                    break;
                case CONTEXT:
                    break;
                case CALLBACK:
                    throw new ProcessingException("CDI for callbacks is not yet supported");
                case RESOURCE:
                    throw new ProcessingException("CDI for resources is not yet supported");
                }
            }
        } catch (IntrospectionException e) {
            throw new ProcessingException(e);
        }
    }

    public boolean injectionAnnotationsPresent(Annotation[][] annots) {
        for (Annotation[] annotations : annots) {
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotType = annotation.annotationType();
                if (annotType.equals(Property.class)
                        || annotType.equals(Reference.class)
                        || annotType.equals(Resource.class)) {
                    return true;
                }
            }
        }
        return false;
    }

    public ReferenceDefinition createReference(String name, InjectionSite injectionSite, Class<?> paramType)
            throws ProcessingException {
        ServiceContract<Type> contract;
        try {
            contract = contractProcessor.introspect(paramType);
        } catch (InvalidServiceContractException e1) {
            throw new ProcessingException(e1);
        }
        ReferenceDefinition reference = new ReferenceDefinition(name, contract);
        reference.setRequired(false);
        return reference;
    }

    /**
     * Determines if all the members of a collection have unique types
     *
     * @param collection the collection to analyze
     * @param start      the position in the collection to start
     * @return true if the types are unique
     */
    private boolean areUnique(Class[] collection, int start) {
        Object compare = collection[start];
        for (int i = start + 1; i < collection.length; i++) {
            if (compare.equals(collection[i])) {
                return false;
            }
        }
        return start + 1 >= collection.length || areUnique(collection, start + 1);
    }

    private void processProperty(Property annotation, Constructor<?> constructor, int index, PojoComponentType componentType) throws IntrospectionException, ProcessingException {
        String name = helper.getSiteName(constructor, index, annotation == null ? "" : annotation.name());
        if (componentType.getProperties().containsKey(name)) {
            throw new DuplicatePropertyException(name);
        }
        Class<?> type = helper.getType(constructor, index);
        InjectionSite injectionSite = new ConstructorInjectionSite(constructor, index);
        org.fabric3.scdl.Property<?> property = createDefinition(annotation, name, type);
        componentType.add(property, injectionSite);
    }

    private <T> org.fabric3.scdl.Property<T> createDefinition(Property annotation, String name, Class<T> type) {
        org.fabric3.scdl.Property<T> property = new org.fabric3.scdl.Property<T>();
        property.setName(name);
        property.setJavaType(type);
        property.setRequired(annotation == null || annotation.required());
        property.setMany(helper.isManyValued(type));
        return property;
    }

    private void processReference(Reference annotation, Constructor<?> constructor, int index, PojoComponentType componentType) throws IntrospectionException, ProcessingException {
        String name = helper.getSiteName(constructor, index, annotation == null ? "" : annotation.name());
        if (componentType.getReferences().containsKey(name)) {
            throw new DuplicateReferenceException(name);
        }
        Type type = helper.getGenericType(constructor, index);
        InjectionSite injectionSite = new ConstructorInjectionSite(constructor, index);
        ReferenceDefinition reference = createDefinition(name, annotation == null || annotation.required(), type);
        componentType.add(reference, injectionSite);
    }

    private ReferenceDefinition createDefinition(String name, boolean required, Type type) throws IntrospectionException {
        ServiceContract<Type> contract = contractProcessor.introspect(helper.getBaseType(type));
        Multiplicity multiplicity = multiplicity(required, type);

        ReferenceDefinition reference = new ReferenceDefinition(name, contract, multiplicity);
        reference.setRequired(required);
        return reference;
    }

    private Multiplicity multiplicity(boolean required, Type type) {
        if (helper.isManyValued(type)) {
            return required ? Multiplicity.ONE_N : Multiplicity.ZERO_N;
        } else {
            return required ? Multiplicity.ONE_ONE : Multiplicity.ZERO_ONE;
        }
    }


    private void processMonitor(Constructor<?> constructor, int index, PojoComponentType componentType) throws IntrospectionException {
        Type type = helper.getGenericType(constructor, index);
        ServiceContract<?> serviceContract = contractProcessor.introspect(type);
        String name = serviceContract.getInterfaceName();
        MonitorResource resource = new MonitorResource(name, false, serviceContract);
        InjectionSite injectionSite = new ConstructorInjectionSite(constructor, index);
        componentType.add(resource, injectionSite);
    }
}
