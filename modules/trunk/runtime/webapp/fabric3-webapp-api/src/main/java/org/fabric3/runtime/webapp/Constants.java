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

/**
 * Configuration paramterts used by the web application runtime bootstrap process.
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
     * Name of the servlet context-param that should contain the component id for the webapp.
     */
    public static final String COMPONENT_PARAM = "fabric3.component";

    /**
     * Servlet context-param name for user-specified application SCDL path.
     */
    public static final String APPLICATION_COMPOSITE_PATH_PARAM = "fabric3.applicationCompositePath";

    /**
     * Default application composite path.
     */
    public static final String APPLICATION_COMPOSITE_PATH_DEFAULT = "/WEB-INF/web.composite";

    /**
     * Name of the context attribute that contains the ComponentContext.
     */
    public static final String CONTEXT_ATTRIBUTE = "org.osoa.sca.ComponentContext";

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
     * Context attribute to which the Fabric3 runtime for this servlet context is stored.
     */
    public static final String RUNTIME_ATTRIBUTE = "fabric3.runtime";

    /**
     * Name of the parameter that defines the class to load to launch the runtime.
     */
    public static final String MONITOR_FACTORY_PARAM = "fabric3.monitorFactory";

    /**
     * Name of the default webapp runtime implementation.
     */
    public static final String MONITOR_FACTORY_DEFAULT = "org.fabric3.monitor.impl.JavaLoggingMonitorFactory";

    /**
     * Default monitor configuration file path found in the webapp.
     */
    public static final String MONITOR_CONFIG_PATH = "/WEB-INF/monitor.properties";

    private Constants() {
    }
}
