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
package org.fabric3.spring.xml;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.spring.SpringComponentType;
import org.fabric3.spring.SpringImplementation;

/**
 * @version $Rev$ $Date$
 */
public class SpringComponentTypeLoaderImpl implements SpringComponentTypeLoader {

    public SpringComponentTypeLoaderImpl() {
    }

    public void load(SpringImplementation implementation, IntrospectionContext introspectionContext) throws LoaderException {
        SpringComponentType componentType = implementation.getComponentType();
//        componentType = loadByIntrospection(implementation, introspectionContext);
        if (componentType.getScope() == null) {
            componentType.setScope("STATELESS");
        }
        implementation.setComponentType(componentType);
    }

//    protected PojoComponentType loadByIntrospection(SpringImplementation implementation, IntrospectionContext context)
//            throws ProcessingException, MissingResourceException {
//        Class<?> implClass = null;
////                LoaderUtil.loadClass(implementation.getLocation(), context.getTargetClassLoader());
//        PojoComponentType componentType = new PojoComponentType(implClass.getName());
//        introspector.introspect(implClass, componentType, context);
//        return componentType;
//    }
}
