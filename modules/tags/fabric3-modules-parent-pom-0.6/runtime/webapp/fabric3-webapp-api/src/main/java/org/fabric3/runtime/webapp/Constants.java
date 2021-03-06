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

/**
 * Constants used by the web application booter
 *
 * @version $Rev$ $Date$
 */
public final class Constants {

    /**
     * Default management domain.
     */
    public static final String DEFAULT_MANAGEMENT_DOMAIN = "webapp-host";

    /**
     * Name of the servlet context-param that should contain the JMX management domain.
     */
    public static final String MANAGEMENT_DOMAIN_PARAM = "fabric3.management.domain";

    /**
     * Name of the servlet context-param that should contain the component id for the webapp.
     */
    public static final String DOMAIN_PARAM = "fabric3.domain";

    /**
     * Name of the servlet context-param that should contain the component target namespace for the webapp.
     */
    public static final String COMPOSITE_NAMESPACE_PARAM = "fabric3.compositeNamespace";

    /**
     * Name of the servlet context-param that should contain the component id for the webapp.
     */
    public static final String COMPOSITE_PARAM = "fabric3.composite";

    /**
     * Servlet context-param name for the base runtime directory.
     */
    public static final String BASE_DIR = "fabric3.baseDir";

    /**
     * Name of the servlet context-param that should contain the component id for the webapp.
     */
    public static final String COMPONENT_PARAM = "fabric3.component";

    /**
     * Servlet context-param name for user-specified application SCDL path.
     */
    public static final String APPLICATION_SCDL_PATH_PARAM = "fabric3.applicationScdlPath";

    /**
     * Default application SCDL path.
     */
    public static final String APPLICATION_SCDL_PATH_DEFAULT = "/WEB-INF/web.composite";

    /**
     * Servlet context-param name for setting if the runtime is online.
     */
    public static final String ONLINE_PARAM = "fabric3.online";

    /**
     * Name of the context attribute that contains the ComponentContext.
     */
    public static final String CONTEXT_ATTRIBUTE = "org.osoa.sca.ComponentContext";

    /**
     * Name of the parameter that defines the class to load to launch the runtime.
     */
    public static final String RUNTIME_PARAM = "fabric3.runtimeImpl";

    /**
     * Name of the default webapp runtime implementation.
     */
    public static final String RUNTIME_DEFAULT = "org.fabric3.runtime.webapp.WebappRuntimeImpl";

    /**
     * Name of the parameter that defines whether the work scheduler should pause on start.
     */
    public static final String PAUSE_ON_START_PARAM = "fabric3.work.scheduler.pauseOnStart";

    /**
     * The default pause on start value.
     */
    public static final String PAUSE_ON_START_DEFAULT = "false";

    /**
     * Name of the parameter that defines the number of worker threads.
     */
    public static final String NUM_WORKERS_PARAM = "fabric3.work.scheduler.numWorkers";

    /**
     * The number of default worker threads.
     */
    public static final String NUM_WORKERS_DEFAULT = "10";

    /**
     * Name of the parameter that defines the class to load to bootstrap the runtime.
     */
    public static final String BOOTSTRAP_PARAM = "fabric3.bootstrapImpl";

    /**
     * Name of the default webapp bootstrap implementation.
     */
    public static final String BOOTSTRAP_DEFAULT = "org.fabric3.fabric.runtime.bootstrap.ScdlBootstrapperImpl";

    /**
     * Name of the parameter that defines the class to load to coordinate booting the runtime.
     */
    public static final String COORDINATOR_PARAM = "fabric3.coordinatorImpl";

    /**
     * Name of the default webapp coordinator implementation.
     */
    public static final String COORDINATOR_DEFAULT = "org.fabric3.fabric.runtime.DefaultCoordinator";


    /**
     * Servlet context-param name for user-specified system SCDL path.
     */
    public static final String SYSTEM_SCDL_PATH_PARAM = "fabric3.systemScdlPath";

    /**
     * Default webapp system SCDL path.
     */
    public static final String SYSTEM_SCDL_PATH_DEFAULT = "META-INF/fabric3/webapp.composite";

    /**
     * Servlet context-param name for user-specified intents file path.
     */
    public static final String INTENTS_PATH_PARAM = "fabric3.intentsPath";

    /**
     * Default webapp system intents file path.
     */
    public static final String INTENTS_PATH_DEFAULT = "META-INF/fabric3/intents.xml";

    /**
     * Context attribute to which the Fabric3 runtime for this servlet context is stored.
     */
    public static final String RUNTIME_ATTRIBUTE = "fabric3.runtime";

    /**
     * Servlet context-param name for the path to the composite to set as the webb app composite
     */
    public static final String CURRENT_COMPOSITE_PATH_PARAM = "fabric3.currentCompositePath";

    /**
     * Servlet context-param name for system monitoring level. Supported values are the names of statics defined in java.util.logging.Level.
     */
    public static final String SYSTEM_MONITORING_PARAM = "fabric3.monitoringLevel";

    /**
     * Default log level
     */
    public static final String SYSTEM_MONITORING_DEFAULT = "FINEST";

    /**
     * Name of the parameter that defines the class to load to launch the runtime.
     */
    public static final String MONITOR_FACTORY_PARAM = "fabric3.monitorFactory";

    /**
     * Name of the default webapp runtime implementation.
     */
    public static final String MONITOR_FACTORY_DEFAULT = "org.fabric3.monitor.impl.JavaLoggingMonitorFactory";

    /**
     * Parameter that defines the log formatter
     */
    public static final String LOG_FORMATTER_PARAM = "fabric3.jdkLogFormatter";

    /**
     * Name of the default log formatter
     */
    public static final String LOG_FORMATTER_DEFAULT = "org.fabric3.monitor.impl.Fabric3LogFormatter";
    /**
     * Name of bundle files used for monitoring messages
     */
    public static final String MONITORING_BUNDLE_PARAM = "fabric3.monitoringBundle";

    /**
     * Default name of bundle files for monitoring messages
     */
    public static final String MONITORING_BUNDLE_DEFAULT = "f3";

    private Constants() {
    }
}
