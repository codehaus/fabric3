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
package org.fabric3.runtime.webapp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.namespace.QName;

import org.fabric3.host.Fabric3RuntimeException;
import org.fabric3.host.Names;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.contribution.ValidationException;
import org.fabric3.host.domain.AssemblyException;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.runtime.ShutdownException;
import static org.fabric3.runtime.webapp.Constants.APPLICATION_COMPOSITE_PATH_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.APPLICATION_COMPOSITE_PATH_PARAM;
import static org.fabric3.runtime.webapp.Constants.COMPONENT_PARAM;
import static org.fabric3.runtime.webapp.Constants.COMPOSITE_NAMESPACE_PARAM;
import static org.fabric3.runtime.webapp.Constants.COMPOSITE_PARAM;
import static org.fabric3.runtime.webapp.Constants.DEFAULT_MANAGEMENT_DOMAIN;
import static org.fabric3.runtime.webapp.Constants.DOMAIN_PARAM;
import static org.fabric3.runtime.webapp.Constants.MANAGEMENT_DOMAIN_PARAM;
import static org.fabric3.runtime.webapp.Constants.POLICY_PARAM;
import static org.fabric3.runtime.webapp.Constants.RUNTIME_ATTRIBUTE;

/**
 * Launches a Fabric3 runtime in a web application, loading information from servlet context parameters. This listener manages one runtime per servlet
 * context; the lifecycle of that runtime corresponds to the the lifecycle of the associated servlet context.
 * <p/>
 * The <code>web.xml</code> of a web application embedding Fabric3 must have entries for this listener and {@link Fabric3ContextListener}. The latter
 * notifies the runtime of session creation and expiration events through a "bridging" contract, {@link WebappRuntime}.
 *
 * @version $Rev$ $Date$
 */
public class Fabric3ContextListener implements ServletContextListener {

    private RuntimeLifecycleCoordinator<WebappRuntime, Bootstrapper> coordinator;

    public void contextInitialized(ServletContextEvent event) {

        ClassLoader webappClassLoader = Thread.currentThread().getContextClassLoader();
        ServletContext servletContext = event.getServletContext();
        WebappUtil utils = getUtils(servletContext);
        WebappRuntime runtime;
        WebAppMonitor monitor = null;
        try {
            String defaultComposite = "WebappComposite";
            String compositeNamespace = utils.getInitParameter(COMPOSITE_NAMESPACE_PARAM, null);
            String compositeName = utils.getInitParameter(COMPOSITE_PARAM, defaultComposite);
            URI componentId = new URI(utils.getInitParameter(COMPONENT_PARAM, "webapp"));
            String scdlPath = utils.getInitParameter(APPLICATION_COMPOSITE_PATH_PARAM, APPLICATION_COMPOSITE_PATH_DEFAULT);
            URL scdl = servletContext.getResource(scdlPath);
            if (scdl == null) {
                throw new InitializationException("Web composite not found");
            }
            runtime = createRuntime(webappClassLoader, servletContext, utils);
            monitor = runtime.getMonitorFactory().getMonitor(WebAppMonitor.class);
            coordinator = utils.getCoordinator(webappClassLoader);
            BootConfiguration<WebappRuntime, Bootstrapper> configuration = createBootConfiguration(runtime, webappClassLoader, utils);
            coordinator.setConfiguration(configuration);

            // boot the runtime
            coordinator.bootPrimordial();
            coordinator.initialize();
            Future<Void> future = coordinator.recover();
            future.get();
            future = coordinator.joinDomain(-1);
            future.get();
            future = coordinator.start();
            future.get();
            servletContext.setAttribute(RUNTIME_ATTRIBUTE, runtime);
            monitor.started(runtime.getJMXSubDomain());
            // deploy the application composite
            QName qName = new QName(compositeNamespace, compositeName);
            runtime.deploy(qName, componentId);
            monitor.compositeDeployed(qName);
        } catch (ValidationException e) {
            // print out the validation errors
            monitor.contributionErrors(e.getMessage());
            throw new Fabric3InitException("Errors were detected in the web application contribution");
        } catch (AssemblyException e) {
            // print out the deployment errors
            monitor.deploymentErrors(e.getMessage());
            throw new Fabric3InitException("Deployment errors were detected");
        } catch (Fabric3RuntimeException e) {
            if (monitor != null) {
                monitor.runError(e);
            }
            throw e;
        } catch (Throwable e) {
            if (monitor != null) {
                monitor.runError(e);
            }
            throw new Fabric3InitException(e);
        }
    }

    private WebappRuntime createRuntime(ClassLoader webappClassLoader, ServletContext context, WebappUtil utils) {

        try {

            File baseDir = new File(URLDecoder.decode(context.getResource("/WEB-INF/lib/").getFile(), "UTF-8"));
            File tempDir = new File(System.getProperty("java.io.tmpdir"), ".f3");
            tempDir.mkdir();
            URI domain = new URI(utils.getInitParameter(DOMAIN_PARAM, "fabric3://domain"));
            WebappHostInfo info = new WebappHostInfoImpl(context, domain, baseDir, tempDir);

            WebappRuntime runtime = utils.getRuntime(webappClassLoader);
            runtime.setHostInfo(info);
            runtime.setHostClassLoader(webappClassLoader);
            String managementDomain = utils.getInitParameter(MANAGEMENT_DOMAIN_PARAM, DEFAULT_MANAGEMENT_DOMAIN);
            runtime.setJmxSubDomain(managementDomain);

            return runtime;

        } catch (URISyntaxException e) {
            throw new Fabric3InitException(e);
        } catch (UnsupportedEncodingException e) {
            throw new Fabric3InitException(e);
        } catch (MalformedURLException e) {
            throw new Fabric3InitException(e);
        }

    }

    /*
     * Creates the boot configuration.
     */
    private BootConfiguration<WebappRuntime, Bootstrapper> createBootConfiguration(WebappRuntime runtime,
                                                                                   ClassLoader webappClassLoader,
                                                                                   WebappUtil utils) throws InitializationException {

        BootConfiguration<WebappRuntime, Bootstrapper> configuration = new BootConfiguration<WebappRuntime, Bootstrapper>();
        configuration.setBootClassLoader(webappClassLoader);

        // create the runtime bootrapper
        URL systemScdl = utils.getSystemScdl(webappClassLoader);
        ScdlBootstrapper bootstrapper = utils.getBootstrapper(webappClassLoader);
        bootstrapper.setScdlLocation(systemScdl);
        configuration.setBootstrapper(bootstrapper);

        Map<String, String> exportedPackages = new HashMap<String, String>();
        exportedPackages.put("javax.naming", "0.0");
        exportedPackages.put("javax.security.auth", "0.0");
        exportedPackages.put("org.fabric3.spi.*", Names.VERSION);
        exportedPackages.put("org.fabric3.host.*", Names.VERSION);
        exportedPackages.put("org.fabric3.management.*", Names.VERSION);
        exportedPackages.put("org.fabric3.model.*", Names.VERSION);
        exportedPackages.put("org.fabric3.pojo.*", Names.VERSION);
        exportedPackages.put("org.fabric3.runtime.webapp", Names.VERSION);
        exportedPackages.put("org.fabric3.container.web.spi", Names.VERSION);
        configuration.setExportedPackages(exportedPackages);

        // process extensions
        ServletContext context = runtime.getHostInfo().getServletContext();
        List<ContributionSource> extensions = getExtensionContributions("/WEB-INF/lib/f3Extensions.properties", context);
        configuration.setExtensions(extensions);

        // process the baseline intents
        URL intentsLocation = utils.getIntentsLocation(webappClassLoader);
        if (intentsLocation == null) {
            intentsLocation = webappClassLoader.getResource("META-INF/fabric3/intents.xml");
        }
        ContributionSource source = new FileContributionSource(Names.CORE_INTENTS_CONTRIBUTION, intentsLocation, -1, new byte[0]);
        configuration.setIntents(source);
        configuration.setRuntime(runtime);

        String policies = utils.getInitParameter(POLICY_PARAM, null);
        if (policies != null) {
            List<ContributionSource> policyContributions = new LinkedList<ContributionSource>();
            StringTokenizer tok = new StringTokenizer(policies);
            int i = 0;
            while (tok.hasMoreElements()) {
                String policy = tok.nextToken();
                URL policyUrl = webappClassLoader.getResource(policy);
                URI uri = URI.create(Names.USER_POLICY_CONTRIBUTION.toASCIIString() + i++);
                policyContributions.add(new FileContributionSource(uri, policyUrl, -1, new byte[0]));
            }
            configuration.setPolicies(policyContributions);
        }

        return configuration;

    }

    /*
     * Gets the extension contributions.
     */
    private List<ContributionSource> getExtensionContributions(String extensionDefinitions, ServletContext context) throws InitializationException {

        InputStream stream = context.getResourceAsStream(extensionDefinitions);
        if (stream == null) {
            return Collections.emptyList();
        }

        Properties props = new Properties();
        try {
            props.load(stream);
        } catch (IOException e) {
            throw new InitializationException(e);
        }

        List<URL> files = new ArrayList<URL>();
        for (Object key : props.keySet()) {
            try {
                URL url = context.getResource("/WEB-INF/lib/" + key).toURI().toURL();
                files.add(url);
            } catch (MalformedURLException e) {
                throw new AssertionError(e);
            } catch (URISyntaxException e) {
                throw new AssertionError(e);
            }
        }

        if (!files.isEmpty()) {
            // contribute and activate extensions if they exist in the runtime domain
            List<ContributionSource> sources = new ArrayList<ContributionSource>();
            for (URL location : files) {
                URI uri = URI.create(location.getPath());
                ContributionSource source = new FileContributionSource(uri, location, -1, new byte[0]);
                sources.add(source);

            }
            return sources;
        }

        return Collections.emptyList();

    }

    /**
     * Can be overridden for tighter host integration.
     *
     * @param servletContext Servlet context for the runtime.
     * @return Webapp util to be used.
     */
    protected WebappUtil getUtils(ServletContext servletContext) {
        return new WebappUtilImpl(servletContext);
    }

    /**
     * Invoked when the servlet context is destroyed. This is used to shutdown the runtime.
     */
    public void contextDestroyed(ServletContextEvent event) {

        ServletContext servletContext = event.getServletContext();
        WebappRuntime runtime = (WebappRuntime) servletContext.getAttribute(RUNTIME_ATTRIBUTE);

        if (runtime != null) {
            servletContext.removeAttribute(RUNTIME_ATTRIBUTE);
            runtime.getMonitorFactory().getMonitor(WebAppMonitor.class).stopped();
        }

        try {
            if (coordinator == null) {
                return;
            }
            Future<Void> future = coordinator.shutdown();
            future.get();
        } catch (ShutdownException e) {
            servletContext.log("Error shutting runtume down", e);
        } catch (ExecutionException e) {
            servletContext.log("Error shutting runtume down", e);
        } catch (InterruptedException e) {
            servletContext.log("Error shutting runtume down", e);
        }

    }


}
