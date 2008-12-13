/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.binding.ws.metro.runtime.core;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;

/**
 * Custom servlet delegate that supports lazy initiation of adapters.
 */
public class F3ServletDelegate extends WSServletDelegate {

    private Map<String, ServletAdapter> adapters = new ConcurrentHashMap<String, ServletAdapter>();
    private Map<String, ClassLoader> classLoaders = new ConcurrentHashMap<String, ClassLoader>();

    /**
     * Initialises an empty list of adapters.
     *
     * @param servletContext Servlet context.
     */
    public F3ServletDelegate(ServletContext servletContext) {
        super(new ArrayList<ServletAdapter>(), servletContext);
    }

    /**
     * Registers a new servlet adapter. Each adapter corresponds to a provisioned service.
     *
     * @param servletAdapter Servlet adapter to be registsred.
     * @param classLoader    the TCCL classloader to set on incoming requests
     */
    public void registerServletAdapter(ServletAdapter servletAdapter, ClassLoader classLoader) {
        final String path = servletAdapter.urlPattern;
        adapters.put(path, servletAdapter);
        classLoaders.put(path, classLoader);
    }

    /**
     * Does a hash lookup. Currently supports only exact paths.
     */
    @Override
    protected ServletAdapter getTarget(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return adapters.get(path);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext)
            throws ServletException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        ClassLoader classLoader = classLoaders.get(path);
        assert classLoader != null;
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // set the TCCL to the service endpoint classloader
            Thread.currentThread().setContextClassLoader(classLoader);
            super.doPost(request, response, servletContext);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}
