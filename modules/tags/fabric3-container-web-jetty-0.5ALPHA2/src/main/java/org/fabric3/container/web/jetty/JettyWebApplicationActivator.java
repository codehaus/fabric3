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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.mortbay.jetty.webapp.WebAppContext;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.container.web.spi.WebApplicationActivationException;
import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.jetty.JettyService;
import org.fabric3.pojo.reflection.Injector;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.ArtifactResolverRegistry;

/**
 * Activates a web application in an embedded Jetty instance.
 *
 * @version $Revision$ $Date$
 */
public class JettyWebApplicationActivator implements WebApplicationActivator {
    private JettyService jettyService;
    private ClassLoaderRegistry classLoaderRegistry;
    private ArtifactResolverRegistry resolverRegistry;
    private WebApplicationActivatorMonitor monitor;
    private Map<URL, WebAppContext> mappings;

    public JettyWebApplicationActivator(@Reference JettyService jettyService,
                                        @Reference ClassLoaderRegistry classLoaderRegistry,
                                        @Reference ArtifactResolverRegistry resolverRegistry,
                                        @Monitor WebApplicationActivatorMonitor monitor) {
        this.jettyService = jettyService;
        this.monitor = monitor;
        this.classLoaderRegistry = classLoaderRegistry;
        this.resolverRegistry = resolverRegistry;
        mappings = new ConcurrentHashMap<URL, WebAppContext>();
    }

    public ClassLoader getWebComponentClassLoader(URI componentId) {
        return classLoaderRegistry.getClassLoader(componentId);
    }

    @SuppressWarnings({"unchecked"})
    public ServletContext activate(String contextPath,
                                   URL url,
                                   URI parentClassLoaderId,
                                   Map<String, List<Injector<?>>> injectors,
                                   ComponentContext componentContext) throws WebApplicationActivationException {
        if (mappings.containsKey(url)) {
            throw new WebApplicationActivationException("Mapping already exists: " + url.toString());
        }
        try {
            // resolve the url to a local artifact
            URL resolved = resolverRegistry.resolve(url);
            ClassLoader parentClassLoader = createParentClassLoader(parentClassLoaderId, url.toURI());
            WebAppContext context = createWebAppContext("/" + contextPath, injectors, resolved, parentClassLoader);
            jettyService.registerHandler(context);  // the context needs to be registered before it is started
            context.start();
            // Setup the session listener to inject conversational reference proxies in newly created sessions
            // Note the listener must be added after the context is started as Jetty web xml configurer clears event listeners
            List<Injector<HttpSession>> sessionInjectors = List.class.cast(injectors.get(SESSION_CONTEXT_SITE));
            InjectingSessionListener listener = new InjectingSessionListener(sessionInjectors);
            context.getSessionHandler().addEventListener(listener);
            ServletContext servletContext = context.getServletContext();
            injectServletContext(servletContext, injectors);
            mappings.put(url, context);
            monitor.activated(url);
            return servletContext;
        } catch (Exception e) {
            throw new WebApplicationActivationException(e);
        }
    }

    public void deactivate(URL url) throws WebApplicationActivationException {
        WebAppContext context = mappings.get(url);
        if (context == null) {
            throw new WebApplicationActivationException("Mapping does not exist: " + url.toString());
        }
        jettyService.getServer().removeLifeCycle(context);
        monitor.deactivated(url);
    }

    private ClassLoader createParentClassLoader(URI parentClassLoaderId, URI id) {
        ClassLoader cl = classLoaderRegistry.getClassLoader(parentClassLoaderId);
        MultiParentClassLoader parentClassLoader = new MultiParentClassLoader(id, cl);
        // we need to make user and web container extensions available for JSP compilation
        parentClassLoader.addParent(getClass().getClassLoader());
        return parentClassLoader;
    }

    private WebAppContext createWebAppContext(String contextPath,
                                              Map<String, List<Injector<?>>> injectors,
                                              URL resolved, ClassLoader parentClassLoader) throws IOException, URISyntaxException {
        WebAppContext context = new WebAppContext(resolved.toExternalForm(), contextPath);
        context.setParentLoaderPriority(true);

        context.setServletHandler(new InjectingServletHandler(injectors));
        WebAppClassLoader webAppClassLoader;
        webAppClassLoader = new WebAppClassLoader(parentClassLoader, context);
        context.setClassLoader(webAppClassLoader);
        context.addHandler(new WorkContextHandler());
        return context;
    }

    @SuppressWarnings({"unchecked"})
    private void injectServletContext(ServletContext servletContext, Map<String, List<Injector<?>>> injectors) throws ObjectCreationException {
        List<Injector<?>> list = injectors.get(SERVLET_CONTEXT_SITE);
        if (list == null) {
            // nothing to inject
            return;
        }
        for (Injector injector : list) {
            injector.inject(servletContext);
        }
    }

}
