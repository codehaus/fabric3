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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Remotable;
import org.osoa.sca.annotations.Service;
import org.osoa.sca.annotations.ConversationID;
import org.osoa.sca.annotations.Context;
import org.osoa.sca.annotations.Callback;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.InvalidServiceContractException;
import org.fabric3.introspection.java.TypeMapping;
import org.fabric3.pojo.processor.ImplementationProcessorExtension;
import org.fabric3.pojo.processor.ImplementationProcessorService;
import static org.fabric3.pojo.processor.JavaIntrospectionHelper.getAllInterfaces;
import static org.fabric3.pojo.processor.JavaIntrospectionHelper.getAllPublicAndProtectedFields;
import static org.fabric3.pojo.processor.JavaIntrospectionHelper.getAllUniquePublicProtectedMethods;
import static org.fabric3.pojo.processor.JavaIntrospectionHelper.toPropertyName;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.Signature;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.ReferenceDefinition;

/**
 * Heuristically evaluates an un-annotated Java implementation type to determine services, references, and properties according to the algorithm
 * described in the SCA Java Client and Implementation Model Specification <p/> TODO Implement:
 * <p/>
 * When no service inteface is annotated, need to calculate a single service comprising all public methods that are not reference or property
 * injection sites. If that service can be exactly mapped to an interface implemented by the class then the service interface will be defined in terms
 * of that interface.
 *
 * @version $Rev$ $Date$
 */
public class HeuristicPojoProcessor extends ImplementationProcessorExtension {
    private ImplementationProcessorService implService;

    public HeuristicPojoProcessor(@Reference ImplementationProcessorService service) {
        this.implService = service;
    }

    public <T> void visitEnd(
            Class<T> clazz,
            PojoComponentType type,
            IntrospectionContext context) throws ProcessingException {
        Map<String, ServiceDefinition> services = type.getServices();
        if (services.isEmpty()) {
            // heuristically determine the service
            // TODO finish algorithm
            Set<Class> interfaces = getAllInterfaces(clazz);
            if (interfaces.size() == 0) {
                // class is the interface
                ServiceDefinition service;
                try {
                    service = implService.createService(clazz, context.getTypeMapping());
                } catch (InvalidServiceContractException e) {
                    throw new ProcessingException(e);
                }
                type.getServices().put(service.getName(), service);
            } else if (interfaces.size() == 1) {
                ServiceDefinition service;
                try {
                    service = implService.createService(interfaces.iterator().next(), context.getTypeMapping());
                } catch (InvalidServiceContractException e) {
                    throw new ProcessingException(e);
                }
                type.getServices().put(service.getName(), service);
            }
        }
        Set<Method> methods = getAllUniquePublicProtectedMethods(clazz);

        // if no references or properties have been defined infer them from the implementation
        if (type.getReferences().isEmpty() && type.getProperties().isEmpty() && type.getResources().isEmpty()) {
            calcPropRefs(methods, services, type, clazz, context.getTypeMapping());
        }

        // if no services have been defined, infer them from the implementation
        if (type.getServices().isEmpty()) {
            calculateServiceInterface(clazz, type, methods, context.getTypeMapping());
            if (type.getServices().isEmpty()) {
                throw new ServiceTypeNotFoundException(clazz.getName());
            }
        }

        if (type.getConstructor() == null) {
            evaluateConstructor(type, clazz, context);
        }
    }

    private void calcPropRefs(Set<Method> methods,
                                  Map<String, ServiceDefinition> services,
                                  PojoComponentType type,
                                  Class<?> clazz,
                                  TypeMapping typeMapping) throws ProcessingException {
        // heuristically determine the properties references
        // make a first pass through all public methods with one param
        for (Method method : methods) {
            if (method.getParameterTypes().length != 1 || !Modifier.isPublic(method.getModifiers())
                    || !method.getName().startsWith("set")
                    || method.getReturnType() != void.class) {
                continue;
            }
            if (method.isAnnotationPresent(ConversationID.class) ||
                    method.isAnnotationPresent(Context.class) ||
                    method.isAnnotationPresent(Callback.class)) {
                // hack to avoid interpreting this method as a property
                continue;
            }
            if (!isInServiceInterface(method, services)) {
                String name = toPropertyName(method.getName());
                // avoid duplicate property or ref names
                if (!type.getProperties().containsKey(name) && !type.getReferences().containsKey(name)) {
                    Class<?> param = method.getParameterTypes()[0];
                    Type genericType = method.getGenericParameterTypes()[0];
                    InjectionSite site = new MethodInjectionSite(method, 0);
                    if (isReferenceType(genericType)) {
                        type.add(implService.createReference(name, param, typeMapping), site);
                    } else {
                        type.add(createProperty(name), site);
                    }
                }
            }
        }
        // second pass for protected methods with one param
        for (Method method : methods) {
            if (method.getParameterTypes().length != 1 || !Modifier.isProtected(method.getModifiers())
                    || !method.getName().startsWith("set")
                    || method.getReturnType() != void.class) {
                continue;
            }
            if (method.isAnnotationPresent(ConversationID.class) ||
                    method.isAnnotationPresent(Context.class) ||
                    method.isAnnotationPresent(Callback.class)) {
                // hack to avoid interpreting this method as a property
                continue;
            }
            Class<?> param = method.getParameterTypes()[0];
            String name = toPropertyName(method.getName());
            // avoid duplicate property or ref names
            if (!type.getProperties().containsKey(name) && !type.getReferences().containsKey(name)) {
                InjectionSite site = new MethodInjectionSite(method, 0);
                if (isReferenceType(param)) {
                    type.add(implService.createReference(name, param, typeMapping), site);
                } else {
                    type.add(createProperty(name), site);
                }
            }
        }
        Set<Field> fields = getAllPublicAndProtectedFields(clazz);
        for (Field field : fields) {
            if (field.isAnnotationPresent(ConversationID.class) ||
                    field.isAnnotationPresent(Context.class) ||
                    field.isAnnotationPresent(Callback.class)) {
                // hack to avoid interpreting this method as a property
                continue;
            }
            Class<?> paramType = field.getType();
            InjectionSite site = new FieldInjectionSite(field);
            if (isReferenceType(paramType)) {
                type.add(implService.createReference(field.getName(), paramType, typeMapping), site);
            } else {
                type.add(createProperty(field.getName()), site);
            }
        }
    }

    /**
     * Determines the constructor to use based on the component type's references and properties
     *
     * @param type  the component type
     * @param clazz the implementation class corresponding to the component type
     * @throws NoConstructorException        if no suitable constructor is found
     * @throws AmbiguousConstructorException if the parameters of a constructor cannot be unambiguously mapped to references and properties
     */
    @SuppressWarnings("unchecked")
    private <T> void evaluateConstructor(PojoComponentType type, Class<T> clazz, IntrospectionContext context) throws ProcessingException {
        // heuristically determine constructor
        Constructor[] constructors = clazz.getConstructors();
        if (constructors.length == 0) {
            throw new NoConstructorException("No public constructor for class", clazz.getName());
        }

        Constructor constructor;
        if (constructors.length == 1) {
            constructor = constructors[0];
        } else {
            try {
                constructor = clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new NoConstructorException();
            }
        }
        type.setConstructor(new Signature(constructor));

        Class[] params = constructor.getParameterTypes();
        if (params.length == 0) {
            return;
        }

        implService.processParameters(constructor, type, context);
    }

    /*
     * Returns true if a given type is reference according to the SCA specification rules for determining reference
     * types
     */
    private boolean isReferenceType(Type operationType) {
        Class<?> rawType;
        Class<?> referenceType = null;
        if (operationType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) operationType;
            rawType = (Class<?>) parameterizedType.getRawType();
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            if (typeArgs.length == 1) {
                referenceType = (Class<?>) typeArgs[0];
            }
        } else {
            rawType = (Class<?>) operationType;
        }
        if (rawType.isArray()) {
            referenceType = rawType.getComponentType();
        } else if (Collection.class.isAssignableFrom(rawType) && referenceType == null) {
            return true;
        }
        if (referenceType != null) {
            return referenceType.getAnnotation(Remotable.class) != null
                    || referenceType.getAnnotation(Service.class) != null;
        } else {
            return rawType.getAnnotation(Remotable.class) != null || rawType.getAnnotation(Service.class) != null;
        }
    }

    /*
     * Returns true if the given operation is defined in the collection of service interfaces
     */
    private boolean isInServiceInterface(Method operation, Map<String, ServiceDefinition> services) {
        for (ServiceDefinition service : services.values()) {
            String interfaze = service.getServiceContract().getQualifiedInterfaceName();
            try {
                // Class<?> clazz = Class.forName(interfaze);
                // TODO The classloader needs to be passed in
                Class<?> clazz = operation.getDeclaringClass().getClassLoader().loadClass(interfaze);
                if (hasOperation(clazz, operation)) {
                    return true;
                }
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        }
        return false;
    }

    private boolean hasOperation(Class interfaceClass, Method operation) {
        if (operation.getDeclaringClass().equals(interfaceClass)) {
            return true;
        }
        Method[] methods = interfaceClass.getMethods();
        for (Method method : methods) {
            if (operation.getName().equals(method.getName())
                    && operation.getParameterTypes().length == method.getParameterTypes().length) {
                Class<?>[] methodTypes = method.getParameterTypes();
                for (int i = 0; i < operation.getParameterTypes().length; i++) {
                    Class<?> paramType = operation.getParameterTypes()[i];
                    if (!paramType.equals(methodTypes[i])) {
                        break;
                    } else if (i == operation.getParameterTypes().length - 1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /*
     * Creates a mapped property
     *
     * @param name      the property name
     * @param member    the injection site the reference maps to
     * @param paramType the property type
     */
    private <T> Property createProperty(String name) {
        Property property = new Property();
        property.setName(name);
        return property;
    }

    /*
     * Populates a component type with a service whose interface type is determined by examining all implemented
     * interfaces of the given class and chosing one whose operations match all of the class's non-property and
     * non-reference methods
     *
     * @param clazz   the class to examine
     * @param type    the component type
     * @param methods all methods in the class to examine
     */
    private void calculateServiceInterface(
            Class<?> clazz,
            PojoComponentType type,
            Set<Method> methods,
            TypeMapping typeMapping) throws ProcessingException {
        List<Method> nonPropRefMethods = new ArrayList<Method>();
        // Map<String, JavaMappedService> services = type.getServices();
        Map<String, ReferenceDefinition> references = type.getReferences();
        Map<String, Property> properties = type.getProperties();
        // calculate methods that are not properties or references
        for (Method method : methods) {
            String name = toPropertyName(method.getName());
            if (!references.containsKey(name) && !properties.containsKey(name)) {
                nonPropRefMethods.add(method);
            }
        }
        // determine if an implemented interface matches all of the non-property and non-reference methods
        Class[] interfaces = clazz.getInterfaces();
        if (interfaces.length == 0) {
            return;
        }
        for (Class interfaze : interfaces) {
            if (analyzeInterface(interfaze, nonPropRefMethods)) {
                ServiceDefinition service;
                try {
                    service = implService.createService(interfaze, typeMapping);
                } catch (InvalidServiceContractException e) {
                    throw new ProcessingException(e);
                }
                type.getServices().put(service.getName(), service);
            }
        }
    }

    /**
     * Determines if the methods of a given interface match the given list of methods
     *
     * @param interfaze         the interface to examine
     * @param nonPropRefMethods the list of methods to match against
     * @return true if the interface matches
     */
    private boolean analyzeInterface(Class<?> interfaze, List<Method> nonPropRefMethods) {
        Method[] interfaceMethods = interfaze.getMethods();
        if (nonPropRefMethods.size() != interfaceMethods.length) {
            return false;
        }
        for (Method method : nonPropRefMethods) {
            boolean found = false;
            for (Method interfaceMethod : interfaceMethods) {
                if (interfaceMethod.getName().equals(method.getName())) {
                    Class<?>[] interfaceParamTypes = interfaceMethod.getParameterTypes();
                    Class<?>[] methodParamTypes = method.getParameterTypes();
                    if (interfaceParamTypes.length == methodParamTypes.length) {
                        if (interfaceParamTypes.length == 0) {
                            found = true;
                        } else {
                            for (int i = 0; i < methodParamTypes.length; i++) {
                                Class<?> param = methodParamTypes[i];
                                if (!param.equals(interfaceParamTypes[i])) {
                                    break;
                                }
                                if (i == methodParamTypes.length - 1) {
                                    found = true;
                                }
                            }
                        }
                    }
                    if (found) {
                        break;
                    }
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}

/*
 * 1) public setter methods that are not included in any service interface 2) protected setter methods 3) public or
 * protected fields unless there is a setter method for the same name If the type associated with the member is an array
 * or a java.util.Collection, then the basetype will be the element type of the array or the parameterized type of the
 * Collection, otherwise the basetype will be the member type. If the basetype is an interface with an @Remotable or
 * @Service annotation then the member will be defined as a reference, otherwise it will be defined as a property.
 * 
 * 
 */
