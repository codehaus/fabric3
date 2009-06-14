/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
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
