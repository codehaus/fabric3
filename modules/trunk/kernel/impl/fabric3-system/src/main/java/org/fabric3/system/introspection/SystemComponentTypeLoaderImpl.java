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
package org.fabric3.system.introspection;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.ImplementationNotFoundException;
import org.fabric3.introspection.java.IntrospectionHelper;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.MissingResourceException;
import org.fabric3.pojo.processor.Introspector;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Scope;
import org.fabric3.system.scdl.SystemImplementation;

/**
 * Loads a system component type
 *
 * @version $Rev$ $Date$
 */
public class SystemComponentTypeLoaderImpl implements SystemComponentTypeLoader {
    private final Introspector introspector;
    private final IntrospectionHelper helper;

    public SystemComponentTypeLoaderImpl(@Reference Introspector introspector,
                                         @Reference IntrospectionHelper helper) {
        this.introspector = introspector;
        this.helper = helper;
    }

    public void load(SystemImplementation implementation, IntrospectionContext introspectionContext) throws LoaderException {
        PojoComponentType componentType = loadByIntrospection(implementation, introspectionContext);
        // this means system components are always composite scoped
        componentType.setImplementationScope(Scope.COMPOSITE);
        implementation.setComponentType(componentType);
    }

    protected PojoComponentType loadByIntrospection(SystemImplementation implementation, IntrospectionContext context)
            throws LoaderException {
        try {
            ClassLoader cl = context.getTargetClassLoader();
            Class<?> implClass = helper.loadClass(implementation.getImplementationClass(), cl);
            PojoComponentType componentType = new PojoComponentType(implClass.getName());
            introspector.introspect(implClass, componentType, context);
            return componentType;
        } catch (ImplementationNotFoundException e) {
            throw new MissingResourceException(null, implementation.getImplementationClass(), e);
        }
    }
}
