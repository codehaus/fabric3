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
package org.fabric3.fabric.implementation.composite;

import java.net.URI;
import java.net.URL;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.loader.LoaderContextImpl;
import org.fabric3.spi.deployer.CompositeClassLoader;
import org.fabric3.spi.loader.Loader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.CompositeImplementation;

/**
 * Loads a composite component type
 *
 * @version $Rev$ $Date$
 */
public class CompositeComponentTypeLoaderImpl implements CompositeComponentTypeLoader {
    private final Loader loader;

    public CompositeComponentTypeLoaderImpl(@Reference LoaderRegistry loaderRegistry) {
        this.loader = loaderRegistry;
    }

    public void load(CompositeImplementation implementation, LoaderContext context) throws LoaderException {
        URL scdlLocation = context.getSourceBase();
        if (scdlLocation == null) {
            throw new LoaderException("SCDL location not found");
        }
        // JFM leave URI null for now as we will be removing the classloader usage
        ClassLoader cl = new CompositeClassLoader(URI.create("test"), context.getTargetClassLoader());
        LoaderContext childContext =
                new LoaderContextImpl(cl, scdlLocation);
        CompositeComponentType componentType = loadFromSidefile(scdlLocation, childContext);
        implementation.setComponentType(componentType);
    }

    protected CompositeComponentType loadFromSidefile(URL url, LoaderContext context) throws LoaderException {
        return loader.load(url, CompositeComponentType.class, context);
    }
}
