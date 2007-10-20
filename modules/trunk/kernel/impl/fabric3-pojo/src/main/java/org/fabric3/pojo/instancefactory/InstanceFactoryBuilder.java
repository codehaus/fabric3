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
package org.fabric3.pojo.instancefactory;

import org.fabric3.spi.component.InstanceFactoryProvider;

/**
 * Interface for building instance factories.
 *
 * @version $Revision$ $Date$
 * @param <IFP> the type of instance factory provider this implementation builds
 * @param <IFD> the type of definition this implementation builds from
 */
public interface InstanceFactoryBuilder<IFP extends InstanceFactoryProvider, IFD extends InstanceFactoryDefinition> {

    /**
     * Builds an instance factory provider from a definition.
     *
     * @param ifd the definition that describes the provider
     * @param cl  the classloader to use to load any implementation classes
     * @return a provider built from the supplied definition
     * @throws InstanceFactoryBuilderException if there was a problem with the definition
     */
    IFP build(IFD ifd, ClassLoader cl) throws InstanceFactoryBuilderException;
}
