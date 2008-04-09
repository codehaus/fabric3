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
package org.fabric3.web.runtime;

import javax.servlet.ServletContext;

import org.fabric3.pojo.reflection.Injector;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;

/**
 * Injects objects (reference proxies, properties, contexts) into a ServletContext.
 *
 * @version $Revision$ $Date$
 */
public class ServletContextInjector implements Injector<ServletContext> {
    private ObjectFactory<?> objectFactory;
    private String key;

    public void inject(ServletContext context) throws ObjectCreationException {
        context.setAttribute(key, objectFactory.getInstance());
    }

    public void setObectFactory(ObjectFactory<?> objectFactory, Object key) {
        this.objectFactory = objectFactory;
        this.key = key.toString();
    }
}
