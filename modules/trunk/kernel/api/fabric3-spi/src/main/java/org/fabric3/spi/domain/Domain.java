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
package org.fabric3.spi.domain;

import javax.xml.namespace.QName;

import org.fabric3.scdl.Composite;

/**
 * Represents a domain.
 *
 * @version $Rev$ $Date$
 */
public interface Domain {

    /**
     * Initializes the domain.
     *
     * @throws DomainException if an error occurs initializing the domain
     */
    void initialize() throws DomainException;

    /**
     * Include a deployable composite in the domain.
     *
     * @param deployable the name of the deployable composite to include
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(QName deployable) throws DeploymentException;

    /**
     * Include a Composite in the domain.
     *
     * @param composite the composite to include
     * @throws DeploymentException if an error is encountered during inclusion
     */
    void include(Composite composite) throws DeploymentException;


    /**
     * Remove a deployable Composite from the domain.
     *
     * @param deployable the name of the deployable composite to remove
     * @throws DeploymentException if an error is encountered during removal
     */
    void remove(QName deployable) throws DeploymentException;

    /**
     * Remove a Composite from the domain.
     *
     * @param composite the composite to remove
     * @throws DeploymentException if an error is encountered during removal
     */
    void remove(Composite composite) throws DeploymentException;

}
