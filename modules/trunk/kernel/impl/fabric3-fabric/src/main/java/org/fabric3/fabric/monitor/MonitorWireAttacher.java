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
package org.fabric3.fabric.monitor;

import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
public class MonitorWireAttacher implements WireAttacher<PhysicalWireSourceDefinition, MonitorWireTargetDefinition> {
    public void attachToSource(PhysicalWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire)
            throws WiringException {
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, MonitorWireTargetDefinition target, Wire wire)
            throws WiringException {
    }
}
