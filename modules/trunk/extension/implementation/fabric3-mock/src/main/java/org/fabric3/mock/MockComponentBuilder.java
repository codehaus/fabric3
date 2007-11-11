/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.mock;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class MockComponentBuilder<T> implements ComponentBuilder<MockComponentDefinition, MockComponent<T>> {
    
    private static final URI CLASS_LOADER_ID = URI.create("sca://./applicationClassLoader");
    
    private ComponentBuilderRegistry builderRegistry;
    private ClassLoaderRegistry classLoaderRegistry;
    
    public MockComponentBuilder(@Reference ComponentBuilderRegistry builderRegistry,
                                @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.builderRegistry = builderRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
    }
    
    @Init
    public void init() {
        builderRegistry.register(MockComponentDefinition.class, this);
    }

    public MockComponent<T> build(MockComponentDefinition componentDefinition) throws BuilderException {
        
        List<String> interfaces = componentDefinition.getInterfaces();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(CLASS_LOADER_ID);
        
        List<Class<?>> mockedInterfaces = new LinkedList<Class<?>>();
        for(String interfaze : interfaces) {
            try {
                mockedInterfaces.add(classLoader.loadClass(interfaze));
            } catch (ClassNotFoundException ex) {
                throw new AssertionError(ex);
            }
        }

        ObjectFactory<T> objectFactory = new MockObjectFactory<T>(mockedInterfaces, classLoader);
        
        return new MockComponent<T>(componentDefinition.getComponentId(), objectFactory);
        
    }

}
