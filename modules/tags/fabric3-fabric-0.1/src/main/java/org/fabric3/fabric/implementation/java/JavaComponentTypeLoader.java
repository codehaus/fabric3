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
package org.fabric3.fabric.implementation.java;

import java.net.URL;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.util.JavaIntrospectionHelper;
import org.fabric3.pojo.processor.IntrospectionRegistry;
import org.fabric3.pojo.processor.Introspector;
import org.fabric3.pojo.processor.PojoComponentType;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.spi.loader.ComponentTypeLoader;
import org.fabric3.spi.loader.Loader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.Scope;

/**
 * @version $Rev$ $Date$
 */
public class JavaComponentTypeLoader implements ComponentTypeLoader<JavaImplementation> {
    private final Loader loader;
    private final Introspector introspector;


    @Constructor({"registry", "introspector"})
    public JavaComponentTypeLoader(@Reference LoaderRegistry loaderRegistry,
                                   @Reference IntrospectionRegistry introspector) {
        this.loader = loaderRegistry;
        this.introspector = introspector;
    }

    public void load(JavaImplementation implementation, LoaderContext loaderContext) throws LoaderException {
        Class<?> implClass = implementation.getImplementationClass();
        URL resource = implClass.getResource(JavaIntrospectionHelper.getBaseName(implClass) + ".componentType");
        PojoComponentType componentType;
        if (resource == null) {
            componentType = loadByIntrospection(implementation, loaderContext);
        } else {
            componentType = loadFromSidefile(resource, loaderContext);
        }
        if (componentType.getImplementationScope() == null) {
            componentType.setImplementationScope(Scope.STATELESS);
        }
        implementation.setComponentType(componentType);
    }

    protected PojoComponentType loadByIntrospection(JavaImplementation implementation, LoaderContext context)
            throws ProcessingException {
        PojoComponentType componentType = new PojoComponentType();
        Class<?> implClass = implementation.getImplementationClass();
        introspector.introspect(implClass, componentType, context);
        return componentType;
    }

    protected PojoComponentType loadFromSidefile(URL url, LoaderContext loaderContext) throws LoaderException {
        // FIXME we need to merge the loaded componentType information with the introspection result
        throw new UnsupportedOperationException();
/*
        PojoComponentType componentType = new PojoComponentType();
        return loader.load(url, PojoComponentType.class, loaderContext);
*/
    }
}
