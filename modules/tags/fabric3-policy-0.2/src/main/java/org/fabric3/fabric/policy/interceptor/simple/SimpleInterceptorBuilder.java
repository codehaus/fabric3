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
package org.fabric3.fabric.policy.interceptor.simple;

import org.fabric3.extension.interceptor.InterceptorBuilderExtension;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.wire.Interceptor;

/**
 * Builder for simple interceptors.
 * 
 * TODO Not sure whether the loader will have the class definition, probably not, 
 * the interceptor builder will need the classloader passed in.
 * 
 * @version $Revision$ $Date$
 */
public class SimpleInterceptorBuilder extends InterceptorBuilderExtension<SimpleInterceptorDefinition, Interceptor> {

    /**
     * @see org.fabric3.extension.interceptor.InterceptorBuilderExtension#getInterceptorDefinitionClass()
     */
    @Override
    protected Class<SimpleInterceptorDefinition> getInterceptorDefinitionClass() {
        return SimpleInterceptorDefinition.class;
    }

    /**
     * @see org.fabric3.spi.builder.interceptor.InterceptorBuilder#build(org.fabric3.spi.model.physical.PhysicalInterceptorDefinition)
     */
    public Interceptor build(SimpleInterceptorDefinition definition) throws BuilderException {
        
        String className = definition.getInterceptorClass();
        
        try {
            @SuppressWarnings("unchecked")
            Class<Interceptor> interceptorClass = (Class<Interceptor>) Class.forName(className);
            return interceptorClass.newInstance();
        } catch (InstantiationException ex) {
            throw new SimpleInterceptorBuilderException("Unable to instantiate", className, ex);            
        } catch (IllegalAccessException ex) {
            throw new SimpleInterceptorBuilderException("Cannot access class or constructor", className, ex);
        } catch (ClassNotFoundException ex) {
            throw new SimpleInterceptorBuilderException("Class not found", className, ex);
        }
        
    }

}
