/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.spi.model.physical;

import java.io.Serializable;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Model class representing the portable definition of a wire.
 * <p/>
 * The definition describes a wire between a source component and a target component, defining how the wire should be attached at both ends. It also
 * describes the operations available on the wire, and whether the connection between the two components can be optimized.
 *
 * @version $Rev$ $Date$
 */
public class PhysicalWireDefinition implements Serializable {
    private static final long serialVersionUID = 995196092611674935L;

    private PhysicalWireSourceDefinition source;
    private PhysicalWireTargetDefinition target;
    private QName sourceDeployable;
    private QName targetDeployable;

    private final Set<PhysicalOperationDefinition> operations;
    private boolean optimizable;

    public PhysicalWireDefinition(PhysicalWireSourceDefinition source,
                                  PhysicalWireTargetDefinition target,
                                  Set<PhysicalOperationDefinition> operations) {
        this.source = source;
        this.target = target;
        this.operations = operations;
    }

    public PhysicalWireDefinition(PhysicalWireSourceDefinition source,
                                  QName sourceDeployable,
                                  PhysicalWireTargetDefinition target,
                                  QName targetDeployable,
                                  Set<PhysicalOperationDefinition> operations) {
        this.source = source;
        this.sourceDeployable = sourceDeployable;
        this.target = target;
        this.operations = operations;
        this.targetDeployable = targetDeployable;
    }

    public QName getSourceDeployable() {
        return sourceDeployable;
    }

    public QName getTargetDeployable() {
        return targetDeployable;
    }

    /**
     * Returns true if the wire can be optimized.
     *
     * @return true if the wire can be optimized
     */
    public boolean isOptimizable() {
        return optimizable;
    }

    /**
     * Sets whether the wire can be optimized.
     *
     * @param optimizable whether the wire can be optimized
     */
    public void setOptimizable(boolean optimizable) {
        this.optimizable = optimizable;
    }

    /**
     * Adds an operation definition.
     *
     * @param operation Operation to be added.
     */
    public void addOperation(PhysicalOperationDefinition operation) {
        operations.add(operation);
    }


    /**
     * Returns the available operations.
     *
     * @return Collection of operations.
     */
    public Set<PhysicalOperationDefinition> getOperations() {
        return operations;
    }

    /**
     * Returns the physical definition for the source side of the wire.
     *
     * @return the physical definition for the source side of the wire
     */
    public PhysicalWireSourceDefinition getSource() {
        return source;
    }

    /**
     * Sets the physical definition for the source side of the wire.
     *
     * @param source the physical definition for the source side of the wire
     */
    public void setSource(PhysicalWireSourceDefinition source) {
        this.source = source;
    }

    /**
     * Returns the physical definition for the target side of the wire.
     *
     * @return the physical definition for the target side of the wire
     */
    public PhysicalWireTargetDefinition getTarget() {
        return target;
    }

    /**
     * Sets the physical definition for the target side of the wire.
     *
     * @param target the physical definition for the target side of the wire
     */
    public void setTarget(PhysicalWireTargetDefinition target) {
        this.target = target;
    }

}
