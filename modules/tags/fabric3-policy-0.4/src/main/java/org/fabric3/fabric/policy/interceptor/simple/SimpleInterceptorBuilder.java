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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.builder.interceptor.InterceptorBuilderRegistry;
import org.fabric3.spi.wire.Interceptor;

/**
 * Builder for simple interceptors.
 * <p/>
 * TODO Not sure whether the loader will have the class definition, probably not, the interceptor builder will need the
 * classloader passed in.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class SimpleInterceptorBuilder implements InterceptorBuilder<SimpleInterceptorDefinition, Interceptor> {
    private InterceptorBuilderRegistry registry;

    public SimpleInterceptorBuilder(@Reference InterceptorBuilderRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(SimpleInterceptorDefinition.class, this);
    }


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
