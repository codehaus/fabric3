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
package org.fabric3.runtime.webapp.activator;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionEvent;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;
import org.osoa.sca.ComponentContext;

import org.fabric3.container.web.spi.WebApplicationActivationException;
import org.fabric3.container.web.spi.WebApplicationActivator;
import org.fabric3.pojo.reflection.Injector;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.ObjectCreationException;

/**
 * A WebApplicationActivator used in a runtime embedded in a WAR.
 *
 * @version $Revision$ $Date$
 */
@Service(interfaces = {WebApplicationActivator.class, HttpSessionListener.class})
public class EmbeddedWebApplicationActivator implements WebApplicationActivator, HttpSessionListener {
    private ServletHost host;
    private List<Injector<HttpSession>> sessionInjectors;

    public EmbeddedWebApplicationActivator(@Reference ServletHost host) {
        this.host = host;
    }

    public ClassLoader getWebComponentClassLoader(URI componentId) {
        //  As the runtime is embedded in a web app, the TCCL is the webapp classloader
        return Thread.currentThread().getContextClassLoader();
    }

    @SuppressWarnings({"unchecked"})
    public ServletContext activate(String contextPath,
                                   URL url,
                                   URI parentClassLoaderId,
                                   Map<String, List<Injector<?>>> injectors,
                                   ComponentContext context) throws WebApplicationActivationException {
        try {
            // the web app has already been activated since it is embedded in a war. Just inject references and properties
            ServletContext servletContext = host.getServletContext();
            injectServletContext(servletContext, injectors);
            servletContext.setAttribute(CONTEXT_ATTRIBUTE, context);
            sessionInjectors = List.class.cast(injectors.get(SESSION_CONTEXT_SITE));
            return servletContext;
        } catch (ObjectCreationException e) {
            throw new WebApplicationActivationException(e);
        }
    }

    public void deactivate(URL url) throws WebApplicationActivationException {
        // do nothing
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

    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        // inject conversational proxies into the session
        for (Injector<HttpSession> injector : sessionInjectors) {
            try {
                injector.inject(session);
            } catch (ObjectCreationException e) {
                throw new RuntimeInjectionException(e);
            }
        }
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        // do nothing
    }
}
