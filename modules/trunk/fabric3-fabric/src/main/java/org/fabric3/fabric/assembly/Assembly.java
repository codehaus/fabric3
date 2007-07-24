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
package org.fabric3.fabric.assembly;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Manages a service network
 *
 * @version $Rev$ $Date$
 */
public interface Assembly {

    /**
     * Initializes the assembly.
     *
     * @throws AssemblyException if an error occurs initializing the assembly
     */
    void initialize() throws AssemblyException;

    /**
     * Returns the domain.
     *
     * @return the domain
     */
    LogicalComponent<CompositeImplementation> getDomain();

    /**
     * Activates a component at the domain level by provisioning physical artifacts to service nodes.
     *
     * @param definition the definition f the component to activate
     * @param include    if true, a domain-level inclusion is performed. That is, for composites, childrent will be
     *                   directly included in the domain and the containing composite will be discarded.
     * @throws ActivateException if an error is encountered during activation
     */
    void activate(ComponentDefinition<?> definition, boolean include) throws ActivateException;

    /**
     * Activates a component at the domain level corresponding to the deployable QName by provisioning physical
     * artifacts to service nodes.
     *
     * @param deployable the deployable QName to activate
     * @param include    if true, a domain-level inclusion is performed. That is, for composites, childrent will be
     *                   directly included in the domain and the containing composite will be discarded.
     * @throws ActivateException if an error is encountered during activation
     */
    void activate(QName deployable, boolean include) throws ActivateException;

    /**
     * Binds a service.
     *
     * @param serviceURI the absolute URI of the service
     * @param binding    the binding definition
     * @throws BindException if an error occurs binding the service
     */
    void bindService(URI serviceURI, BindingDefinition binding) throws BindException;

}
