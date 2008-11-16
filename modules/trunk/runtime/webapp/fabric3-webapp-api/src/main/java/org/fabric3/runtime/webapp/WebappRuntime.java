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

import java.net.URI;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionListener;
import javax.xml.namespace.QName;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.runtime.Fabric3Runtime;

/**
 * The contract for artifacts loaded in the web application classloader to comminicate with the Fabric3 runtime loaded in a child classloader. For
 * example, filters and listeners may use this interface to notify the runtime of the web container events.
 *
 * @version $Rev$ $Date$
 */
public interface WebappRuntime extends ServletRequestListener, HttpSessionListener, Fabric3Runtime<WebappHostInfo> {

    /**
     * Deploys a composite in the domain.
     *
     * @param qName       the composite qualified name
     * @param componentId the id of the component that should be bound to the webapp
     * @throws DeploymentException   if there was a problem initializing the composite
     * @throws ContributionException if an error is found in the contribution. If validation errors are encountered, a ValidationException will be
     *                               thrown.
     */
    void deploy(QName qName, URI componentId) throws ContributionException, DeploymentException;

    /**
     * Returns the ServletRequestInjector for the runtime.
     *
     * @return the ServletRequestInjector for the runtime
     */
    ServletRequestInjector getRequestInjector();

}
