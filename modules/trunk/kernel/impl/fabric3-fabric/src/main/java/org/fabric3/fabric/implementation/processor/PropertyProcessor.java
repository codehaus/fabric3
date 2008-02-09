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

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.pojo.processor.AbstractPropertyProcessor;
import org.fabric3.pojo.scdl.JavaMappedProperty;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.pojo.processor.ImplementationProcessorService;
import org.fabric3.pojo.processor.ProcessingException;

/**
 * Processes an {@link @Property} annotation, updating the component type with corresponding {@link org.fabric3.pojo.scdl.JavaMappedProperty}
 *
 * @version $Rev$ $Date$
 */
public class PropertyProcessor extends AbstractPropertyProcessor<Property> {
    public PropertyProcessor(@Reference ImplementationProcessorService service) {
        super(Property.class, service);
    }

    protected String getName(Property annotation) {
        return annotation.name();
    }

    protected <T> void initProperty(JavaMappedProperty<T> property,
                                    Property annotation,
                                    IntrospectionContext context) {
        property.setRequired(annotation.required());
    }

    public <T> void visitConstructor(Constructor<T> constructor,
                                     PojoComponentType type,
                                     IntrospectionContext context) throws ProcessingException {
        // override since heuristic pojo processor evalautes properties
    }
}
