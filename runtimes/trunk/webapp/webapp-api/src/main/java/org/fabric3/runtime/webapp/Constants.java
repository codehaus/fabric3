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
     * Name of the servlet context-param that should contain the component id for the webapp.
     */
    public static final String DOMAIN_PARAM = "fabric3.domain";

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
    static final String APPLICATION_SCDL_PATH_PARAM = "fabric3.applicationScdlPath";

    /**
     * Default application SCDL path.
     */
    public static final String APPLICATION_SCDL_PATH_DEFAULT = "/WEB-INF/default.scdl";

    /**
     * Servlet context-param name for setting if the runtime is online.
     */
    public static final String ONLINE_PARAM = "fabric3.online";

    /**
     * Name of the context attribute that contains the ComponentContext.
     */
    public static final String CONTEXT_ATTRIBUTE = "fabric3.context";

    /**
     * Name of the parameter that defines the name of webapp resource containing bootstrap jars.
     */
    static final String BOOTDIR_PARAM = "fabric3.bootDir";

    /**
     * Default value for BOOTDIR_PARAM.
     */
    static final String BOOTDIR_DEFAULT = "/WEB-INF/fabric3/boot";

    /**
     * Name of the parameter that defines the class to load to launch the runtime.
     */
    static final String RUNTIME_PARAM = "fabric3.runtimeImpl";

    /**
     * Name of the default webapp runtime implementation.
     */
    static final String RUNTIME_DEFAULT = "org.fabric3.runtime.webapp.WebappRuntimeImpl";

    /**
     * Name of the parameter that defines the class to load to bootstrap the runtime.
     */
    static final String BOOTSTRAP_PARAM = "fabric3.bootstrapImpl";

    /**
     * Name of the default webapp bootstrap implementation.
     */
    static final String BOOTSTRAP_DEFAULT = "org.fabric3.fabric.runtime.ScdlBootstrapperImpl";

    /**
     * Servlet context-param name for user-specified system SCDL path.
     */
    static final String SYSTEM_SCDL_PATH_PARAM = "fabric3.systemScdlPath";

    /**
     * Default webapp system SCDL path.
     */
    static final String SYSTEM_SCDL_PATH_DEFAULT = "META-INF/fabric3/webapp.scdl";

    /**
     * Context attribute to which the Fabric3 runtime for this servlet context is stored.
     */
    static final String RUNTIME_ATTRIBUTE = "fabric3.runtime";

    /**
     * Servlet context-param name for the path to the composite to set as the webb app composite
     */
    static final String CURRENT_COMPOSITE_PATH_PARAM = "fabric3.currentCompositePath";

    /**
     * Servlet context-param name for user-specified system SCDL path.
     */
    static final String EXTENSION_SCDL_PATH_PARAM = "fabric3.extensionScdlPath";

    /**
     * Default path for extensions if no "extensionScdlPath param is specified
     */
    static final String DEFAULT_EXTENSION_PATH_PARAM = "/WEB-INF/fabric3/extensions";
    
    /**
     * Servlet context-param name for system monitoring level. Supported values are the names of statics defined in
     * java.util.logging.Level. If absent, no monitoring will take place.
     */
    static final String SYSTEM_MONITORING_PARAM = "fabric3.monitoringLevel";

    private Constants() {
    }
}
