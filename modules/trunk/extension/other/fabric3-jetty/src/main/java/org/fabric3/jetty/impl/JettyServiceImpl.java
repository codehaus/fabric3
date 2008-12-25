/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
package org.fabric3.jetty.impl;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.jetty.servlet.SessionHandler;
import org.mortbay.log.Log;
import org.mortbay.log.Logger;
import org.mortbay.thread.BoundedThreadPool;
import org.mortbay.thread.ThreadPool;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.work.DefaultPausableWork;
import org.fabric3.host.work.WorkScheduler;
import org.fabric3.jetty.JettyService;

/**
 * Implements an HTTP transport service using Jetty.
 *
 * @version $$Rev$$ $$Date$$
 */
@EagerInit
public class JettyServiceImpl implements JettyService {

    private static final String ROOT = "/";
    private static final int ERROR = 0;
    private static final int UNINITIALIZED = 0;
    private static final int STARTING = 1;
    private static final int STARTED = 2;
    private static final int STOPPING = 3;
    private static final int STOPPED = 4;

    private final Object joinLock = new Object();
    private int state = UNINITIALIZED;
    private int httpPort = 8080;
    private int httpsPort = 8484;
    private String keystore;
    private String certPassword;
    private String keyPassword;
    private boolean sendServerVersion;
    private boolean isHttps;
    private TransportMonitor monitor;
    private WorkScheduler scheduler;
    private boolean debug;
    private Server server;
    private Connector connector;
    private ServletHandler servletHandler;
    private ContextHandlerCollection rootHandler;


    static {
        // hack to replace the static Jetty logger
        System.setProperty("org.mortbay.log.class", JettyLogger.class.getName());
    }


    @Constructor
    public JettyServiceImpl(@Reference WorkScheduler scheduler, @Monitor TransportMonitor monitor) {
        this.monitor = monitor;
        this.scheduler = scheduler;
        // Jetty uses a static logger, so jam in the monitor into a static reference
        Logger logger = Log.getLogger(null);
        if (logger instanceof JettyLogger) {
            JettyLogger jettyLogger = (JettyLogger) logger;
            jettyLogger.setMonitor(this.monitor);
            if (debug) {
                jettyLogger.setDebugEnabled(true);
            }
        }
    }

    public JettyServiceImpl(TransportMonitor monitor) {
        this.monitor = monitor;
    }

    @Property
    public void setHttpPort(String httpPort) {
        this.httpPort = Integer.parseInt(httpPort);
    }

    @Property
    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }

    @Property
    public void setSendServerVersion(boolean sendServerVersion) {
        this.sendServerVersion = sendServerVersion;
    }

    @Property
    public void setHttps(boolean https) {
        this.isHttps = https;
    }

    @Property
    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    @Property
    public void setCertPassword(String certPassword) {
        this.certPassword = certPassword;
    }

    @Property
    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    @Property
    public void setDebug(boolean val) {
        debug = val;
    }

    @Init
    public void init() throws JettyInitializationException {
        try {
            state = STARTING;
            server = new Server();
            initializeThreadPool();
            initializeConnector();
            initializeRootContextHandler();
            server.setStopAtShutdown(true);
            server.setSendServerVersion(sendServerVersion);
            monitor.extensionStarted();
            monitor.startHttpListener(httpPort);
            if (isHttps) {
                monitor.startHttpsListener(httpsPort);
            }
            server.start();
            state = STARTED;
        } catch (Exception e) {
            state = ERROR;
            throw new JettyInitializationException("Error starting Jetty service", e);
        }
    }

    @Destroy
    public void destroy() throws Exception {
        state = STOPPING;
        synchronized (joinLock) {
            joinLock.notifyAll();
        }
        server.stop();
        state = STOPPED;
        monitor.extensionStopped();
    }

    public ServletContext getServletContext() {
        return servletHandler.getServletContext();
    }

    public void registerMapping(String path, Servlet servlet) {
        ServletHolder holder = new ServletHolder(servlet);
        servletHandler.addServlet(holder);
        ServletMapping mapping = new ServletMapping();
        mapping.setServletName(holder.getName());
        mapping.setPathSpec(path);
        servletHandler.addServletMapping(mapping);
    }

    public Servlet unregisterMapping(String string) {
        return null;
    }

    public boolean isMappingRegistered(String path) {
        throw new UnsupportedOperationException();
    }

    public Server getServer() {
        return server;
    }

    public void registerHandler(Handler handler) {
        rootHandler.addHandler(handler);
    }

    public int getHttpPort() {
        return httpPort;
    }

    private void initializeConnector() {
        if (connector == null) {
            if (isHttps) {
                Connector httpConnector = new SelectChannelConnector();
                httpConnector.setPort(httpPort);
                SslSocketConnector sslConnector = new SslSocketConnector();
                sslConnector.setPort(httpsPort);
                sslConnector.setKeystore(keystore);
                sslConnector.setPassword(certPassword);
                sslConnector.setKeyPassword(keyPassword);
                server.setConnectors(new Connector[]{httpConnector, sslConnector});
            } else {
                SelectChannelConnector selectConnector = new SelectChannelConnector();
                selectConnector.setPort(httpPort);
                selectConnector.setSoLingerTime(-1);
                server.setConnectors(new Connector[]{selectConnector});
            }
        } else {
            connector.setPort(httpPort);
            connector.setMaxIdleTime(-1);
            connector.setLowResourceMaxIdleTime(-1);
            server.setConnectors(new Connector[]{connector});
        }
    }

    private void initializeThreadPool() {
        if (scheduler == null) {
            BoundedThreadPool threadPool = new BoundedThreadPool();
            threadPool.setMaxThreads(100);
            server.setThreadPool(threadPool);
        } else {
            server.setThreadPool(new Fabric3ThreadPool());
        }
    }

    private void initializeRootContextHandler() {
        // setup the root context handler which dispatches to other contexts based on the servlet path
        rootHandler = new ContextHandlerCollection();
        server.setHandler(rootHandler);
        ContextHandler contextHandler = new ContextHandler(rootHandler, ROOT);
        SessionHandler sessionHandler = new SessionHandler();
        servletHandler = new ServletHandler();
        sessionHandler.addHandler(servletHandler);
        contextHandler.addHandler(sessionHandler);
    }

    /**
     * An integration wrapper to enable use of a {@link WorkScheduler} with Jetty
     */
    private class Fabric3ThreadPool implements ThreadPool {

        public boolean dispatch(Runnable job) {
            scheduler.scheduleWork(new Fabric3Work(job));
            return true;
        }

        public void join() throws InterruptedException {
            synchronized (joinLock) {
                joinLock.wait();
            }
        }

        public int getThreads() {
            throw new UnsupportedOperationException();
        }

        public int getIdleThreads() {
            throw new UnsupportedOperationException();
        }

        public boolean isLowOnThreads() {
            // TODO FIXME
            return false;
        }

        public void start() throws Exception {

        }

        public void stop() throws Exception {

        }

        public boolean isRunning() {
            return state == STARTING || state == STARTED;
        }

        public boolean isStarted() {
            return state == STARTED;
        }

        public boolean isStarting() {
            return state == STARTING;
        }

        public boolean isStopping() {
            return state == STOPPING;
        }

        public boolean isFailed() {
            return state == ERROR;
        }
    }

    /**
     * A unit of work dispatched to the runtime work scheduler
     */
    private class Fabric3Work extends DefaultPausableWork {

        Runnable job;

        public Fabric3Work(Runnable job) {
            this.job = job;
        }

        public void release() {
        }

        public void execute() {
            job.run();
        }
    }

}
