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

import java.net.URL;

import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.ComponentTypeLoaderExtension;
import org.fabric3.fabric.util.JavaIntrospectionHelper;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.implementation.java.IntrospectionRegistry;
import org.fabric3.spi.implementation.java.Introspector;
import org.fabric3.spi.implementation.java.PojoComponentType;
import org.fabric3.spi.implementation.java.ProcessingException;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.Scope;

/**
 * Loads a system component type
 *
 * @version $Rev$ $Date$
 */
public class SystemComponentTypeLoader extends ComponentTypeLoaderExtension<SystemImplementation> {
    private Introspector introspector;

    public SystemComponentTypeLoader() {
    }

    public SystemComponentTypeLoader(Introspector introspector) {
        this.introspector = introspector;
    }

    public SystemComponentTypeLoader(LoaderRegistry loaderRegistry, Introspector introspector) {
        super(loaderRegistry);
        this.introspector = introspector;
    }

    @Reference
    public void setIntrospector(IntrospectionRegistry introspector) {
        this.introspector = introspector;
    }

    public void load(
        SystemImplementation implementation,
        LoaderContext loaderContext) throws LoaderException {
        Class<?> implClass = implementation.getImplementationClass();
        URL sidefile = implClass.getResource(JavaIntrospectionHelper.getBaseName(implClass) + ".componentType");
        PojoComponentType componentType;
        if (sidefile == null) {
            componentType = loadByIntrospection(implementation, loaderContext);
        } else {
            componentType = loadFromSidefile(sidefile, loaderContext);
        }
        // this means system components are always composite scoped
        componentType.setImplementationScope(Scope.COMPOSITE);
        implementation.setComponentType(componentType);
    }

    protected Class<SystemImplementation> getImplementationClass() {
        return SystemImplementation.class;
    }

    protected PojoComponentType loadByIntrospection(SystemImplementation implementation, LoaderContext context)
        throws ProcessingException {
        PojoComponentType componentType =
            new PojoComponentType();
        Class<?> implClass = implementation.getImplementationClass();
        introspector.introspect(implClass, componentType, context);
        return componentType;
    }


    protected PojoComponentType loadFromSidefile(URL url, LoaderContext loaderContext) throws LoaderException {
        return loaderRegistry.load(null, url, PojoComponentType.class, loaderContext);
    }
}
