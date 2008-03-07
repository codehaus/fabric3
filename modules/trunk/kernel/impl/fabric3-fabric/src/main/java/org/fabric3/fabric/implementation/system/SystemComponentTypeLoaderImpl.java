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
package org.fabric3.fabric.implementation.system;

import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.processor.Introspector;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Scope;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingResourceException;

/**
 * Loads a system component type
 *
 * @version $Rev$ $Date$
 */
public class SystemComponentTypeLoaderImpl implements SystemComponentTypeLoader {
    private final Introspector introspector;

    public SystemComponentTypeLoaderImpl(@Reference Introspector introspector) {
        this.introspector = introspector;
    }

    public void load(SystemImplementation implementation, IntrospectionContext introspectionContext) throws LoaderException {
        PojoComponentType componentType = loadByIntrospection(implementation, introspectionContext);
        // this means system components are always composite scoped
        componentType.setImplementationScope(Scope.COMPOSITE);
        implementation.setComponentType(componentType);
    }

    protected PojoComponentType loadByIntrospection(SystemImplementation implementation, IntrospectionContext context)
            throws ProcessingException, MissingResourceException {
        ClassLoader cl = context.getTargetClassLoader();
        Class<?> implClass = LoaderUtil.loadClass(implementation.getImplementationClass(), cl);
        PojoComponentType componentType = new PojoComponentType(implClass.getName());
        introspector.introspect(implClass, componentType, context);
        return componentType;
    }
}
