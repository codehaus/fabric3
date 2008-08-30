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
package org.fabric3.binding.tcp.runtime.wire;

import org.fabric3.binding.tcp.provision.TCPWireTargetDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * TODO: TCP binding for Reference yet to be implemented.
 * @version $Revision$ $Date$
 */
public class TCPTargetWireAttacher implements TargetWireAttacher<TCPWireTargetDefinition> {

    /**
     * {@inheritDoc}
     */
    public void attachToTarget(PhysicalWireSourceDefinition source, TCPWireTargetDefinition target, Wire wire) throws WiringException {

        new UnsupportedOperationException("TCP binding for Reference yet to be implemented");
    }

    /**
     * {@inheritDoc}
     */
    public ObjectFactory<?> createObjectFactory(TCPWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

}
