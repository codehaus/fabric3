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
package org.fabric3.fabric.implementation.processor;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.extension.implementation.java.AbstractPropertyProcessor;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.spi.implementation.java.ImplementationProcessorService;
import org.fabric3.spi.implementation.java.JavaMappedProperty;
import org.fabric3.spi.loader.LoaderContext;

/**
 * Processes an {@link @Monitor} annotation, updating the component type with corresponding {@link
 * org.fabric3.spi.implementation.java.JavaMappedProperty}
 *
 * @version $Rev$ $Date$
 */
public class MonitorProcessor extends AbstractPropertyProcessor<Monitor> {
    private MonitorFactory monitorFactory;

    public MonitorProcessor(@Reference MonitorFactory factory, @Reference ImplementationProcessorService service) {
        super(Monitor.class, service);
        this.monitorFactory = factory;
    }

    protected String getName(Monitor annotation) {
        return null;
    }

    protected <T> void initProperty(JavaMappedProperty<T> property,
                                    Monitor annotation,
                                    LoaderContext context) {
        Class<T> javaType = property.getJavaType();
        property.setDefaultValueFactory(new SingletonObjectFactory<T>(monitorFactory.getMonitor(javaType)));
    }

}
