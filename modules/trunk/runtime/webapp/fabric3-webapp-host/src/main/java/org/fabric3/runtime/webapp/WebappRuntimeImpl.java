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
package org.fabric3.runtime.webapp;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.fabric3.fabric.runtime.AbstractRuntime;
import org.fabric3.fabric.runtime.ComponentNames;
import static org.fabric3.fabric.runtime.ComponentNames.CONTRIBUTION_SERVICE_URI;
import static org.fabric3.fabric.runtime.ComponentNames.DISTRIBUTED_ASSEMBLY_URI;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.runtime.webapp.contribution.WarContributionSource;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.assembly.Assembly;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.container.web.spi.WebRequestTunnel;

/**
 * Bootstrapper for the Fabric3 runtime in a web application host. This listener manages one runtime per servlet context; the lifecycle of that
 * runtime corresponds to the the lifecycle of the associated servlet context.
 * <p/>
 * The bootstrapper launches the runtime, booting system extensions and applications, according to the servlet parameters defined in {@link
 * Constants}. When the runtime is instantiated, it is placed in the servlet context with the attribute {@link Constants#RUNTIME_PARAM}. The runtime
 * implements {@link WebappRuntime} so that filters and servlets loaded in the parent web app classloader may pass events and requests to it.
 * <p/>
 *
 * @version $$Rev$$ $$Date$$
 */

public class WebappRuntimeImpl extends AbstractRuntime<WebappHostInfo> implements WebappRuntime {
    private ServletRequestInjector requestInjector;
    private HttpSessionListener sessionListener;

    public WebappRuntimeImpl() {
        super(WebappHostInfo.class);
    }

    public void activate(QName qName, URI componentId) throws InitializationException {
        try {
            // contribute the war to the application domain
            Assembly assembly = getSystemComponent(Assembly.class, DISTRIBUTED_ASSEMBLY_URI);
            ContributionService contributionService = getSystemComponent(ContributionService.class, CONTRIBUTION_SERVICE_URI);
            URI contributionUri = new URI("file", qName.getLocalPart(), null);
            WarContributionSource source = new WarContributionSource(contributionUri);
            contributionService.contribute(source);
            // activate the deployable composite in the domain
            assembly.includeInDomain(qName);
        } catch (MalformedURLException e) {
            throw new InitializationException("Invalid web archive", e);
        } catch (ContributionException e) {
            throw new InitializationException("Error processing project", e);
        } catch (ActivateException e) {
            String identifier = qName.toString();
            throw new InitializationException("Error activating composite: " + identifier, identifier, e);
        } catch (URISyntaxException e) {
            throw new InitializationException("Error processing project", e);
        }
    }

    public ServletRequestInjector getRequestInjector() {
        if (requestInjector == null) {
            URI uri = URI.create(ComponentNames.RUNTIME_NAME + "/servletHost");
            requestInjector = getSystemComponent(ServletRequestInjector.class, uri);
        }
        return requestInjector;
    }

    public void requestInitialized(ServletRequestEvent sre) {
        WorkContext workContext = new WorkContext();
        PojoWorkContextTunnel.setThreadWorkContext(workContext);
        ServletRequest req = sre.getServletRequest();
        if (req instanceof HttpServletRequest) {
            WebRequestTunnel.setRequest(((HttpServletRequest) req));
        }
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        PojoWorkContextTunnel.setThreadWorkContext(null);
        WebRequestTunnel.setRequest(null);
    }

    public void sessionCreated(HttpSessionEvent event) {
        getSessionListener().sessionCreated(event);
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        getSessionListener().sessionDestroyed(event);
    }

    private HttpSessionListener getSessionListener() {
        if (sessionListener == null) {
            URI uri = URI.create(ComponentNames.RUNTIME_NAME + "/WebApplicationActivator");
            sessionListener = getSystemComponent(HttpSessionListener.class, uri);
        }
        return sessionListener;
    }
}
