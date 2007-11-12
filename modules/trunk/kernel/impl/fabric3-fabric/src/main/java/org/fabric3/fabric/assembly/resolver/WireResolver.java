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

import org.fabric3.fabric.assembly.ResolutionException;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;

/**
 * Implementations are responsible for resolving wire targets and URIs in an SCA Domain, including autowires.
 *
 * @version $Rev$ $Date$
 */
public interface WireResolver {

    /**
     * Resolves wires for a component definition and its decendents. If component is a composite and the operation is an
     * include, the child references will be resolved against the target composite and the component.
     *
     * @param component the logical component to resolve for
     * @throws ResolutionException if an error occurs during resolution
     */
    void resolve(LogicalComponent<?> component) throws ResolutionException;

    /**
     * Resolves reference targets against the given composite
     *
     * @param logicalReference the reference to resolve
     * @param composite        the composite to resolve against
     * @throws ResolutionException if an error occuurs during resolution
     */
    public void resolveReference(LogicalReference logicalReference, LogicalComponent<CompositeImplementation> composite)
            throws ResolutionException;

}
