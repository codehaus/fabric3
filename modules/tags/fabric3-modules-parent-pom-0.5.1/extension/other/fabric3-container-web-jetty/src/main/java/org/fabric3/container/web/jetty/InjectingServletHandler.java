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
package org.fabric3.container.web.jetty;

import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.mortbay.jetty.servlet.ServletHandler;

import org.fabric3.pojo.reflection.Injector;
import org.fabric3.spi.ObjectCreationException;

/**
 * Injects a servlet or filter with reference proxies, properties, and the component context.
 *
 * @version $Revision$ $Date$
 */
public class InjectingServletHandler extends ServletHandler {
    private Map<String, List<Injector<?>>> injectorMappings;

    public InjectingServletHandler(Map<String, List<Injector<?>>> injectorMappings) {
        this.injectorMappings = injectorMappings;
    }

    public Servlet customizeServlet(Servlet servlet) throws Exception {
        inject(servlet);
        return servlet;
    }

    public Filter customizeFilter(Filter filter) throws Exception {
        inject(filter);
        return filter;
    }

    @SuppressWarnings({"unchecked"})
    private void inject(Object instance) throws ObjectCreationException {
        List<Injector<?>> injectors = injectorMappings.get(instance.getClass().getName());
        if (injectors != null) {
            for (Injector injector : injectors) {
                injector.inject(instance);
            }
        }
    }
}
