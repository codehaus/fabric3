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
package org.fabric3.fabric.instantiator.target;

import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;

/**
 * Abstraction for resolving targets for references. Possible implementations include explicit targets, intent based auto-wiring, and type based
 * auto-wiring. Resolution strategies are exclusive. That is, only one strategy is used per reference.
 *
 * @version $Revision$ $Date$
 */
public interface TargetResolutionService {

    /**
     * Resolves the target for a logical reference.
     *
     * @param reference Logical reference whose target needs to be resolved.
     * @param component Composite component within which the targets are resolved.
     * @param change    the logical change the resolution is performed for. Errors and warnings are reported here.
     */
    void resolve(LogicalReference reference, LogicalCompositeComponent component, LogicalChange change);

}
