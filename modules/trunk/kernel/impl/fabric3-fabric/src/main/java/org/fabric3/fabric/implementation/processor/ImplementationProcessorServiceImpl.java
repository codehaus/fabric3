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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Resource;
import org.fabric3.pojo.processor.DuplicatePropertyException;
import org.fabric3.pojo.processor.ImplementationProcessorService;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.JavaMappedProperty;
import org.fabric3.pojo.scdl.JavaMappedReference;
import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.scdl.MemberSite;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.introspection.ContractProcessor;
import org.fabric3.introspection.InvalidServiceContractException;

/**
 * The default implementation of an <code>ImplementationProcessorService</code>
 *
 * @version $Rev$ $Date$
 */
public class ImplementationProcessorServiceImpl implements ImplementationProcessorService {
    private ContractProcessor registry;

    public ImplementationProcessorServiceImpl(@Reference ContractProcessor registry) {
        this.registry = registry;
    }

    public JavaMappedService createService(Class<?> interfaze) throws InvalidServiceContractException {
        ServiceContract<?> contract = registry.introspect(interfaze);
        return new JavaMappedService(interfaze.getSimpleName(), contract);
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

    public boolean processParam(
            Class<?> param,
            Type genericParam,
            Annotation[] paramAnnotations,
            String[] constructorNames,
            int pos,
            PojoComponentType type,
            List<String> injectionNames) throws ProcessingException {
        boolean processed = false;
        for (Annotation annot : paramAnnotations) {
            if (Property.class.equals(annot.annotationType())) {
                processed = true;
                processProperty(annot, constructorNames, pos, type, param, genericParam, injectionNames);
            } else if (Reference.class.equals(annot.annotationType())) {
                processed = true;
                processReference(annot, constructorNames, pos, type, param, genericParam, injectionNames);
            }/* else if (Resource.class.equals(annot.annotationType())) {
                processed = true;
                processResource((Resource) annot, constructorNames, pos, type, param, injectionNames);
            }*/
        }
        return processed;
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

    public JavaMappedReference createReference(String name, MemberSite member, Class<?> paramType)
            throws ProcessingException {
        ServiceContract<Type> contract;
        try {
            contract = registry.introspect(paramType);
        } catch (InvalidServiceContractException e1) {
            throw new ProcessingException(e1);
        }
        JavaMappedReference reference = new JavaMappedReference(name, contract, member);
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

    /**
     * Processes parameter metadata for a constructor parameter
     *
     * @param annot            the parameter annotation
     * @param constructorNames the parameter names as specified in an {@link org.osoa.sca.annotations.Constructor}
     *                         annotation
     * @param pos              the position of the parameter in the constructor's parameter list
     * @param type             the component type associated with the implementation being processed
     * @param param            the parameter type
     * @param explicitNames    the collection of injection names to update
     * @throws ProcessingException
     */
    @SuppressWarnings("unchecked")
    private void processProperty(
            Annotation annot,
            String[] constructorNames,
            int pos,
            PojoComponentType type,
            Class<?> param,
            Type genericParam,
            List<String> explicitNames) throws ProcessingException {
        // the param is marked as a property
        Property propAnnot = (Property) annot;
        JavaMappedProperty property = new JavaMappedProperty();
        Class<?> baseType = getBaseType(param, genericParam);
        if (param.isArray() || Collection.class.isAssignableFrom(param)) {
            property.setMany(true);
        }
        property.setJavaType(baseType);
        String name = propAnnot.name();
        if (name == null || name.length() == 0) {
            if (constructorNames.length < pos + 1 || constructorNames[pos] == null
                    || constructorNames[pos].length() == 0) {
                throw new InvalidPropertyException("No name specified for property parameter " + (pos + 1));
            }
            name = constructorNames[pos];
        } else if (pos < constructorNames.length && constructorNames[pos] != null
                && constructorNames[pos].length() != 0 && !name.equals(constructorNames[pos])) {
            String paramNum = String.valueOf(pos + 1);
            throw new InvalidConstructorException("Name specified by @Constructor does not match property name",
                                                  paramNum);
        }
        if (type.getProperties().get(name) != null) {
            throw new DuplicatePropertyException(name);
        }
        property.setName(name);
        property.setRequired(propAnnot.required());

        type.getProperties().put(name, property);
        addName(explicitNames, pos, name);
    }

    /**
     * Processes reference metadata for a constructor parameter
     *
     * @param annot            the parameter annotation
     * @param constructorNames the parameter names as specified in an {@link org.osoa.sca.annotations.Constructor}
     *                         annotation
     * @param pos              the position of the parameter in the constructor's parameter list
     * @param type             the component type associated with the implementation being processed
     * @param param            the parameter type
     * @param explicitNames    the collection of injection names to update
     * @throws ProcessingException
     */
    private void processReference(
            Annotation annot,
            String[] constructorNames,
            int pos,
            PojoComponentType type,
            Class<?> param,
            Type genericParam,
            List<String> explicitNames) throws ProcessingException {

        // TODO multiplicity
        // the param is marked as a reference
        Reference refAnnotation = (Reference) annot;
        String name = refAnnotation.name();
        if (name == null || name.length() == 0) {
            if (constructorNames.length == 0 || constructorNames[0].length() == 0) {
                name = "_ref" + pos;
            } else if (constructorNames.length < pos + 1 || constructorNames[pos] == null
                    || constructorNames[pos].length() == 0) {
                throw new InvalidReferenceException("No name specified for reference parameter " + (pos + 1));
            } else {
                name = constructorNames[pos];
            }
        } else if (pos < constructorNames.length && constructorNames[pos] != null
                && constructorNames[pos].length() != 0 && !name.equals(constructorNames[pos])) {
            String paramNum = String.valueOf(pos + 1);
            throw new InvalidConstructorException("Name specified by @Constructor does not match reference name",
                                                  paramNum);
        }
        if (type.getReferences().get(name) != null) {
            throw new DuplicateReferenceException(name);
        }
        boolean required = refAnnotation.required();

        Class<?> baseType = getBaseType(param, genericParam);
        ServiceContract<?> contract;
        try {
            contract = registry.introspect(baseType);
        } catch (InvalidServiceContractException e) {
            throw new ProcessingException(e);
        }
        JavaMappedReference reference = new JavaMappedReference(name, contract, null);
        reference.setRequired(required);
        if (param.isArray() || Collection.class.isAssignableFrom(param) || Map.class.isAssignableFrom(param)) {
            if (required) {
                reference.setMultiplicity(Multiplicity.ONE_N);
            } else {
                reference.setMultiplicity(Multiplicity.ZERO_N);
            }
        } else {
            if (required) {
                reference.setMultiplicity(Multiplicity.ONE_ONE);
            } else {
                reference.setMultiplicity(Multiplicity.ZERO_ONE);
            }
        }
        type.getReferences().put(name, reference);
        addName(explicitNames, pos, name);
    }

    protected static Class<?> getBaseType(Class<?> cls, Type genericType) {
        if (cls.isArray()) {
            return cls.getComponentType();
        } else if (Collection.class.isAssignableFrom(cls)) {
            if (genericType == cls) {
                return Object.class;
            } else {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type baseType = parameterizedType.getActualTypeArguments()[0];
                if (baseType instanceof Class) {
                    return (Class<?>) baseType;
                } else if (baseType instanceof ParameterizedType) {
                    return (Class<?>) ((ParameterizedType) baseType).getRawType();
                } else {
                    return null;
                }
            }
        } else if (Map.class.isAssignableFrom(cls)) {
            if (genericType == cls) {
                return Object.class;
            } else {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type type = parameterizedType.getActualTypeArguments()[1];
                if (type instanceof Class) {
                    return (Class<?>) type;
                } else if (type instanceof ParameterizedType) {
                    ParameterizedType valueType = (ParameterizedType) type;
                    return (Class<?>) valueType.getRawType();
                } else {
                    throw new AssertionError();
                }
            }
        } else {
            return cls;
        }
    }
}
