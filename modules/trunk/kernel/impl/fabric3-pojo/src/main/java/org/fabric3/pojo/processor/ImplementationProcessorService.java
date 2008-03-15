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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;

import org.fabric3.introspection.contract.InvalidServiceContractException;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.helper.TypeMapping;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.ReferenceDefinition;

/**
 * Provides utility methods for Java implementation processing
 *
 * @version $Rev$ $Date$
 */
public interface ImplementationProcessorService {

    /**
     * Introspects the given interface to produce a mapped service
     */
    ServiceDefinition createService(Class<?> interfaze, TypeMapping typeMapping) throws InvalidServiceContractException;

    ReferenceDefinition createReference(String name, Class<?> paramType, TypeMapping typeMapping) throws ProcessingException;

    /**
     * Determines if all the members of a collection have unique types
     *
     * @param collection the collection to analyze
     * @return true if the types are unique
     */
    boolean areUnique(Class[] collection);

    /**
     * Inserts a name at the specified position, paddiling the list if its size is less than the position
     */
    void addName(List<String> names, int pos, String name);

    /**
     * Process constructor parameters.
     *
     * @param constructor the constructor to process
     * @param componentType the componentType to be updated with the results of processing
     * @param context
     * @throws ProcessingException if there was a problem processing the parameters
     */
    void processParameters(Constructor<?> constructor, PojoComponentType componentType, IntrospectionContext context) throws ProcessingException;

    /**
     * Returns true if <code>@Property</code> or <code>@Reference</code> are present in the given array
     *
     * @return true if one of the annotations are present
     */
    boolean injectionAnnotationsPresent(Annotation[][] annots);

}
