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
package org.fabric3.mock;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 */
public class MockSourceWireAttacher implements SourceWireAttacher<MockWireSourceDefinition> {

    public void attachObjectFactory(MockWireSourceDefinition arg0, ObjectFactory<?> arg1, PhysicalWireTargetDefinition definition) throws WiringException {
        // Empty implementation; we don't want to attach anything to the mock
    }

    public void attachToSource(MockWireSourceDefinition arg0, PhysicalWireTargetDefinition arg1, Wire arg2)
            throws WiringException {
        // Empty implementation; we don't want to attach anything to the mock
    }

    public void detachFromSource(MockWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {

    }
}
