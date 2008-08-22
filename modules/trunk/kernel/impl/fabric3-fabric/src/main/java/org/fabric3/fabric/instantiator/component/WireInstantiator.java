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
package org.fabric3.fabric.instantiator.component;

import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.scdl.Composite;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Instantiates explicity wires (i.e. those declared by <wire>) in a composite and its included composites.
 *
 * @version $Revision$ $Date$
 */
public interface WireInstantiator {

    /**
     * Performs the instantiation.
     *
     * @param composite the composite
     * @param parent    the logical composite where the wires will be added
     * @param change    the current logical change set.
     */
    void instantiateWires(Composite composite, LogicalCompositeComponent parent, LogicalChange change);

}
