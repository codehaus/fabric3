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
package org.fabric3.java;

import java.net.URL;

import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.ImplementationNotFoundException;
import org.fabric3.introspection.java.IntrospectionHelper;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.MissingResourceException;
import org.fabric3.pojo.processor.Introspector;
import org.fabric3.pojo.processor.JavaIntrospectionHelper;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Scope;

/**
 * @version $Rev$ $Date$
 */
public class JavaComponentTypeLoaderImpl implements JavaComponentTypeLoader {
    private final Introspector introspector;
    private final IntrospectionHelper helper;

    public JavaComponentTypeLoaderImpl(@Reference Introspector introspector,
                                       @Reference IntrospectionHelper helper) {
        this.introspector = introspector;
        this.helper = helper;
    }

    public void load(JavaImplementation implementation, IntrospectionContext introspectionContext) throws LoaderException {
        Class<?> implClass;
        try {
            implClass = helper.loadClass(implementation.getImplementationClass(), introspectionContext.getTargetClassLoader());
        } catch (ImplementationNotFoundException e) {
            throw new MissingResourceException(null, implementation.getImplementationClass(), e);
        }
        URL resource = implClass.getResource(JavaIntrospectionHelper.getBaseName(implClass) + ".componentType");

        PojoComponentType componentType;
        if (resource == null) {
            componentType = loadByIntrospection(implClass, introspectionContext);
        } else {
            componentType = loadFromSidefile(resource, introspectionContext);
        }
        if (componentType.getImplementationScope() == null) {
            componentType.setImplementationScope(Scope.STATELESS);
        }
        implementation.setComponentType(componentType);
    }

    PojoComponentType loadByIntrospection(Class<?> implClass, IntrospectionContext context) throws ProcessingException {
        PojoComponentType componentType = new PojoComponentType(implClass.getName());
        introspector.introspect(implClass, componentType, context);
        return componentType;
    }

    PojoComponentType loadFromSidefile(URL url, IntrospectionContext introspectionContext) throws LoaderException {
        // FIXME we need to merge the loaded componentType information with the introspection result
        throw new UnsupportedOperationException();
/*
        PojoComponentType componentType = new PojoComponentType();
        return loader.load(url, PojoComponentType.class, introspectionContext);
*/
    }
}
