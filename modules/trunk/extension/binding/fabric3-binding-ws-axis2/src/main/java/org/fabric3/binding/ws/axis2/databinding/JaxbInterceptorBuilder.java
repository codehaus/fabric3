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
package org.fabric3.binding.ws.axis2.databinding;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.builder.interceptor.InterceptorBuilderRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class JaxbInterceptorBuilder implements InterceptorBuilder<JaxbInterceptorDefinition, JaxbInterceptor> {
    
    private InterceptorBuilderRegistry interceptorBuilderRegistry;
    private ClassLoaderRegistry classLoaderRegistry;
    
    public JaxbInterceptorBuilder(@Reference InterceptorBuilderRegistry interceptorBuilderRegistry, 
                                  @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.interceptorBuilderRegistry = interceptorBuilderRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
    }
    
    @Init
    public void init() {
        interceptorBuilderRegistry.register(JaxbInterceptorDefinition.class, this);
    }

    public JaxbInterceptor build(JaxbInterceptorDefinition definition) throws BuilderException {
        
        URI classLoaderId = definition.getClassLoaderId();
        
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(classLoaderId);
        
        try {
            
            List<Class<?>> classes = new LinkedList<Class<?>>();
            
            for (String inClassName : definition.getInClassNames()) {
                classes.add(classLoader.loadClass(inClassName));
            }
            
            classes.add(classLoader.loadClass(definition.getOutClassName()));
            
            return new JaxbInterceptor(classLoader, classes, definition.isService());
            
        } catch (ClassNotFoundException ex) {
            throw new JaxbBuilderException(ex);
        }
        
    }

}
