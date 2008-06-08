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

import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Abstraction for resolving wires.
 *
 * @version $Revision$ $Date$
 */
public interface WiringService {

    /**
     * Wire the component handling promotions of services and references and resolving targets on references.
     *
     * @param logicalComponent Logical component that needs to be wired.
     */
    void wire(LogicalComponent<?> logicalComponent) throws ActivateException;

    /**
     * Handles the promotion on the specified logical service.
     *
     * @param logicalService Logical service whose promotion is handled.
     */
    void promote(LogicalService logicalService) throws PromotionException;

    /**
     * Resolves the target for a logical reference.
     *
     * @param logicalReference Logical reference whose target needs to be resolved.
     * @param context          Composite component within which the targets are resolved.
     * @return True is the target was succesfully involved.
     */
    void wire(LogicalReference logicalReference, LogicalCompositeComponent context) throws ActivateException;
}
