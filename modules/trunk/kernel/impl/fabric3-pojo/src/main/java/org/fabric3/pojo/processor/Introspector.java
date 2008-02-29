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

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.pojo.scdl.PojoComponentType;

/**
 * Implementations are responsible for walking a component implementation class, adding additional component type information as appropriate
 *
 * @version $Rev$ $Date$
 */
public interface Introspector {

    /**
     * Walks the given component implementation class
     *
     * @param clazz   the component implementation class
     * @param type    the component type associated with the implementation class
     * @param context the introspection context
     * @return the updated component type
     * @throws ProcessingException if an error is encountered evaluating the implementation class
     */
    PojoComponentType introspect(Class<?> clazz, PojoComponentType type, IntrospectionContext context) throws ProcessingException;

}
