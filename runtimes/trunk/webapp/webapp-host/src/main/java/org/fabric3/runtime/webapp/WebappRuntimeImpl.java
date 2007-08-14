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

import java.net.URI;
import java.net.URL;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;

import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.assembly.Assembly;
import org.fabric3.fabric.runtime.AbstractRuntime;
import static org.fabric3.fabric.runtime.ComponentNames.DISTRIBUTED_ASSEMBLY_URI;
import static org.fabric3.fabric.runtime.ComponentNames.LOADER_URI;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.loader.common.LoaderContextImpl;
import org.fabric3.runtime.webapp.implementation.webapp.WebappComponent;
import org.fabric3.scdl.Composite;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.loader.Loader;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;

/**
 * Bootstrapper for the Fabric3 runtime in a web application host. This listener manages one runtime per servlet
 * context; the lifecycle of that runtime corresponds to the the lifecycle of the associated servlet context.
 * <p/>
 * The bootstrapper launches the runtime, booting system extensions and applications, according to the servlet
 * parameters defined in {@link Constants}. When the runtime is instantiated, it is placed in the servlet context with
 * the attribute {@link Constants#RUNTIME_PARAM}. The runtime implements {@link WebappRuntime} so that filters and
 * servlets loaded in the parent web app classloader may pass events and requests to it.
 * <p/>
 *
 * @version $$Rev$$ $$Date$$
 */

public class WebappRuntimeImpl extends AbstractRuntime<WebappHostInfo> implements WebappRuntime {
    private ServletContext servletContext;

    private ServletRequestInjector requestInjector;

    public WebappRuntimeImpl() {
        super(WebappHostInfo.class);
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Deprecated
    public void deploy(URI compositeId, URL applicationScdl, URI componentId) throws InitializationException {
        try {
            // load the application scdl
            Loader loader = getSystemComponent(Loader.class, LOADER_URI);
            LoaderContext loaderContext = new LoaderContextImpl(getHostClassLoader(), applicationScdl);
            Composite composite = loader.load(applicationScdl, Composite.class, loaderContext);


            // deploy the components
            Assembly assembly = getSystemComponent(Assembly.class, DISTRIBUTED_ASSEMBLY_URI);
            assembly.includeInDomain(composite);

            URI reslvedUri = URI.create(getHostInfo().getDomain().toString() + "/" + componentId);
            WebappComponent webapp = (WebappComponent) getComponentManager().getComponent(reslvedUri);
            if (webapp == null) {
                throw new Fabric3InitException("No component found with id " + componentId, componentId.toString());
            }
            webapp.bind(getServletContext());

        } catch (LoaderException e) {
            throw new InitializationException(e);
        } catch (ActivateException e) {
            throw new InitializationException(e);
        } catch (ObjectCreationException e) {
            throw new InitializationException(e);
        }
    }

    public ServletRequestInjector getRequestInjector() {
        return requestInjector;
    }

    public void sessionCreated(HttpSessionEvent event) {
/*
        HttpSessionStart startSession = new HttpSessionStart(this, event.getSession().getId());
        application.publish(startSession);
        ((EventPublisher) requestInjector).publish(startSession);
*/
    }

    public void sessionDestroyed(HttpSessionEvent event) {
/*
        HttpSessionEnd endSession = new HttpSessionEnd(this, event.getSession().getId());
        application.publish(endSession);
        ((EventPublisher) requestInjector).publish(endSession);
*/
    }

    public void httpRequestStarted(HttpServletRequest request) {
/*
        HttpSession session = request.getSession(false);
        Object sessionId = session == null ? new LazyHTTPSessionId(request) : session.getId();
        HttpRequestStart httpRequestStart = new HttpRequestStart(this, sessionId);
        application.publish(httpRequestStart);
        ((EventPublisher) requestInjector).publish(httpRequestStart);
*/
    }

    public void httpRequestEnded(Object sessionid) {
/*
        HttpRequestEnded httpRequestEnded = new HttpRequestEnded(this, sessionid);
        application.publish(httpRequestEnded);
        ((EventPublisher) requestInjector).publish(httpRequestEnded);
*/
    }


    public void startRequest() {
/*
        application.publish(new RequestStart(this));
*/
    }

    public void stopRequest() {
/*
        application.publish(new RequestEnd(this));
*/
    }
}
