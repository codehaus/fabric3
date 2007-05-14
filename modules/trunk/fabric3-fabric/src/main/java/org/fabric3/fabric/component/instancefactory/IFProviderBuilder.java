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
package org.fabric3.fabric.component.instancefactory;

import org.fabric3.fabric.component.InstanceFactoryProvider;
import org.fabric3.spi.model.physical.InstanceFactoryProviderDefinition;

/**
 * Interface for building instance factories.
 *
 * @version $Revision$ $Date$
 */
public interface IFProviderBuilder<IFP extends InstanceFactoryProvider,
        IFPD extends InstanceFactoryProviderDefinition> {

    /**
     * Builds an instance factory provider from provider definition.
     *
     * @param ifpd Instance factory provider definition.
     * @param cl   Classloader to use.
     * @return Instance factory provider.
     * @throws IFProviderBuilderException
     */
    IFP build(IFPD ifpd, ClassLoader cl) throws IFProviderBuilderException;
}
