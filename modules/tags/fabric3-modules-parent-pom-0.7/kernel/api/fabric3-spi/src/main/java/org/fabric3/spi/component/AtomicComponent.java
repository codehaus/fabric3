/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
package org.fabric3.spi.component;

import javax.xml.namespace.QName;

import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.invocation.WorkContext;

/**
 * The runtime instantiation of an SCA atomic, or leaf-type, component
 *
 * @version $Rev$ $Date$
 * @param <T> the type of the Java instance associated with this component
 */
public interface AtomicComponent<T> extends Component {
    /**
     * Returns the QName of the deployable composite this component was deployed with.
     *
     * @return the group containing this component
     */
    QName getDeployable();

    /**
     * Returns true if component instances should be eagerly initialized.
     *
     * @return true if component instances should be eagerly initialized
     */
    boolean isEagerInit();

    /**
     * Returns the initialization level for this component.
     *
     * @return the initialization level for this component
     */
    int getInitLevel();

    /**
     * Returns the idle time allowed between operations in milliseconds if the implementation is conversational.
     *
     * @return the idle time allowed between operations in milliseconds if the implementation is conversational
     */
    long getMaxIdleTime();

    /**
     * Returns the maximum age a conversation may remain active in milliseconds if the implementation is conversational.
     *
     * @return the maximum age a conversation may remain active in milliseconds if the implementation is conversational
     */
    long getMaxAge();

    /**
     * Create a new implementation instance, fully injected with all property and reference values. The instance's lifecycle callbacks must not have
     * been called.
     *
     * @param workContext the work context in which to create the instance
     * @return a wrapper for a new implementation instance
     * @throws ObjectCreationException if there was a problem instantiating the implementation
     */
    InstanceWrapper<T> createInstanceWrapper(WorkContext workContext) throws ObjectCreationException;

    /**
     * Create an ObjectFactory that returns an instance of this AtomicComponent.
     *
     * @return an ObjectFactory that returns an instance of this AtomicComponent
     */
    ObjectFactory<T> createObjectFactory();

}
