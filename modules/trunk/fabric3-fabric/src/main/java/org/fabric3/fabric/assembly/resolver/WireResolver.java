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
package org.fabric3.fabric.assembly.resolver;

import java.net.URI;

import org.fabric3.fabric.assembly.ResolutionException;
import org.fabric3.spi.model.type.ServiceContract;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Implementations are responsible for resolving wire targets and URIs in an SCA Domain, including autowires.
 *
 * @version $Rev$ $Date$
 */
public interface WireResolver {

    /**
     * Resolves wires for a component definition and its decendents
     *
     * @param parent    the parent component
     * @param component the logical component to resolve for
     * @throws ResolutionException if an error occurs during resolution
     */
    void resolve(LogicalComponent<?> parent, LogicalComponent<?> component) throws ResolutionException;

    /**
     * Adds the uri of a host system service that can be an autowire target
     *
     * @param contract the service contract of the system service
     * @param uri      the component uri
     */
    void addHostUri(ServiceContract contract, URI uri);

}
