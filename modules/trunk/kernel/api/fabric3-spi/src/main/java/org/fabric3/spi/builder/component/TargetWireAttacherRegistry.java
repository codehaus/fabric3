/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.spi.builder.component;

import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Registration interface for TargetWireAttachers.
 * <p/>
 * Deprecated as it will be removed once we are able to declaratively wire attachers provided by extensions.
 *
 * @version $Rev$ $Date$
 */
@Deprecated
public interface TargetWireAttacherRegistry {
    <PWSD extends PhysicalWireTargetDefinition> void register(Class<PWSD> type, TargetWireAttacher<PWSD> attacher);

    <PWSD extends PhysicalWireTargetDefinition> void unregister(Class<PWSD> type, TargetWireAttacher<PWSD> attacher);
}