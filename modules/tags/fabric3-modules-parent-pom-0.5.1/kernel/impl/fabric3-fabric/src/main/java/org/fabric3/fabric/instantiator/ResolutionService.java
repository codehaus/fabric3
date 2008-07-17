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
package org.fabric3.fabric.instantiator;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Abstraction for resolving reference targets and promotions. Service promotions are resolved by determining the absolute logical URI of the actual
 * service being promoted.  Reference are resolved by determining the absolute logical URIs of the actual references being promoted and the absolute
 * URIs of refeence targets. If references are not explicitly wired, targets may be selected via an autowire algorithm (assuming autowire is enabled).
 * Otherwise, a resolution error results.
 *
 * @version $Revision$ $Date$
 */
public interface ResolutionService {

    /**
     * Resolves promoted references and services as well as reference targets for the logical component. If the component is a composite, its children
     * will be resolved.
     *
     * @param logicalComponent logical component to be resolved.
     * @throws LogicalInstantiationException if a resolution error occurs.
     */
    void resolve(LogicalComponent<?> logicalComponent) throws LogicalInstantiationException;

    /**
     * Resolves the promotion on the specified logical service.
     *
     * @param logicalService Logical service whose promotion is to be resolved.
     * @throws LogicalInstantiationException if a resolution error occurs.
     */
    void resolve(LogicalService logicalService) throws LogicalInstantiationException;

    /**
     * Resolves the logical reference against the given composite.
     *
     * @param logicalReference Logical reference to be resolved.
     * @param composite        Composite component against which the targets are resolved.
     * @throws LogicalInstantiationException if a resolution error occurs.
     */
    void resolve(LogicalReference logicalReference, LogicalCompositeComponent composite) throws LogicalInstantiationException;
}
