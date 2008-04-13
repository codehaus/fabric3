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
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionListener;
import javax.xml.namespace.QName;

import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.InitializationException;

/**
 * The contract for artifacts loaded in the web application classloader to comminicate with the Fabric3 runtime loaded in a child classloader. For
 * example, filters and listeners may use this interface to notify the runtime of the web container events.
 *
 * @version $Rev$ $Date$
 */
public interface WebappRuntime extends ServletRequestListener, HttpSessionListener, Fabric3Runtime<WebappHostInfo> {

    /**
     * Activates a composite in the domain.
     *
     * @param qName       the composite qualified name
     * @param componentId the id of the component that should be bound to the webapp
     * @throws InitializationException if there was a problem initializing the composite
     */
    void activate(QName qName, URI componentId) throws InitializationException;

    /**
     * Returns the ServletRequestInjector for the runtime.
     *
     * @return the ServletRequestInjector for the runtime
     */
    ServletRequestInjector getRequestInjector();

}
