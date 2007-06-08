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
package org.fabric3.groovy;

import java.net.URI;

import org.fabric3.fabric.implementation.pojo.PojoComponent;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.component.ScopeContainer;

/**
 * Runtime container for a component implemented in Groovy.
 * 
 * @version $Rev$ $Date$
 */
public class GroovyComponent<T> extends PojoComponent<T> {
    public GroovyComponent(URI componentId,
                           InstanceFactoryProvider<T> instanceFactoryProvider,
                           ScopeContainer<?> scopeContainer,
                           URI groupId,
                           int initLevel,
                           long maxIdleTime,
                           long maxAge) {
        super(componentId, instanceFactoryProvider, scopeContainer, groupId, initLevel, maxIdleTime, maxAge);
    }
}
