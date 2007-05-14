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

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URI;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import junit.framework.TestCase;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expect;
import static org.easymock.classextension.EasyMock.isA;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import static org.fabric3.runtime.webapp.Constants.APPLICATION_SCDL_PATH_PARAM;
import static org.fabric3.runtime.webapp.Constants.APPLICATION_SCDL_PATH_DEFAULT;
import org.fabric3.host.runtime.ScdlBootstrapper;

/**
 * @version $Rev$ $Date$
 */
public class Fabric3ContextListenerTestCase extends TestCase {
    private String contextName;
    private ServletContext context;
    private Fabric3ContextListener listener;
    private ClassLoader cl;
    private ClassLoader bootClassLoader;
    private URL systemUrl;
    private URL scdl;
    private WebappUtil utils;
    private String compositeId;

    public void testInitializationUsingDefaults() throws Exception {
        ServletContextEvent event = createMock(ServletContextEvent.class);
        expect(event.getServletContext()).andReturn(context);
        replay(event);

        WebappRuntime runtime = createMock(WebappRuntime.class);
        ScdlBootstrapper bootstrapper = createMock(ScdlBootstrapper.class);
        expect(utils.getBootClassLoader(cl)).andReturn(bootClassLoader);
        expect(utils.getInitParameter("fabric3.composite", compositeId)).andReturn(compositeId);
        expect(utils.getInitParameter("fabric3.component", contextName)).andReturn(contextName);
        expect(utils.getInitParameter("fabric3.online", "true")).andReturn("true");
        expect(utils.getInitParameter(APPLICATION_SCDL_PATH_PARAM, APPLICATION_SCDL_PATH_DEFAULT))
            .andReturn(APPLICATION_SCDL_PATH_DEFAULT);
        expect(utils.getRuntime(bootClassLoader)).andReturn(runtime);
        expect(utils.getBootstrapper(bootClassLoader)).andReturn(bootstrapper);
        expect(utils.getSystemScdl(bootClassLoader)).andReturn(systemUrl);
        replay(utils);

        expect(context.getResource("/WEB-INF/fabric3/")).andReturn(null);
        expect(context.getResource(APPLICATION_SCDL_PATH_DEFAULT)).andReturn(scdl);
        context.setAttribute(eq(Constants.RUNTIME_ATTRIBUTE), isA(WebappRuntime.class));
        replay(context);
        replay(cl);
        replay(bootClassLoader);
        expect(listener.getUtils(context)).andReturn(utils);
        replay(listener);
        runtime.setServletContext(context);
        runtime.setRuntimeInfo(isA(WebappHostInfo.class));
        runtime.setHostClassLoader(cl);
        bootstrapper.bootstrap(runtime, bootClassLoader);
        runtime.deploy(URI.create(compositeId), scdl, URI.create(contextName));
        replay(runtime);

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            listener.contextInitialized(event);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
        verify(event);
        verify(utils);
        verify(context);
        verify(listener);
        verify(cl);
        verify(bootClassLoader);
        verify(runtime);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Method getUtilsMethod = Fabric3ContextListener.class.getDeclaredMethod("getUtils", ServletContext.class);
        utils = createMock(WebappUtil.class);
        listener = createMock(Fabric3ContextListener.class, new Method[]{getUtilsMethod});
        context = createMock(ServletContext.class);
        cl = createMock(ClassLoader.class);
        bootClassLoader = createMock(ClassLoader.class);
        systemUrl = new URL("file:/system.scdl");
        scdl = new URL("file:/app.scdl");
        contextName = "webapp";
        compositeId = "webapp";
    }
}
