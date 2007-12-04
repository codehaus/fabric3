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

import java.net.URL;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.processor.IntrospectionRegistry;
import org.fabric3.pojo.processor.Introspector;
import org.fabric3.pojo.processor.JavaIntrospectionHelper;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.MissingResourceException;
import org.fabric3.spring.SpringComponentType;
import org.fabric3.spring.SpringImplementation;

/**
 * @version $Rev$ $Date$
 */
public class SpringComponentTypeLoaderImpl implements SpringComponentTypeLoader {
    private final Introspector introspector;

    @Constructor({"introspector"})
    public SpringComponentTypeLoaderImpl(@Reference IntrospectionRegistry introspector) {
        this.introspector = introspector;
    }

    public void load(SpringImplementation implementation, LoaderContext loaderContext) throws LoaderException {
        SpringComponentType componentType = implementation.getComponentType();
//        componentType = loadByIntrospection(implementation, loaderContext);
        if (componentType.getImplementationScope() == null) {
            componentType.setImplementationScope(Scope.STATELESS);
        }
        implementation.setComponentType(componentType);
    }

//    protected PojoComponentType loadByIntrospection(SpringImplementation implementation, LoaderContext context)
//            throws ProcessingException, MissingResourceException {
//        Class<?> implClass = null;
////                LoaderUtil.loadClass(implementation.getLocation(), context.getTargetClassLoader());
//        PojoComponentType componentType = new PojoComponentType(implClass.getName());
//        introspector.introspect(implClass, componentType, context);
//        return componentType;
//    }
}
