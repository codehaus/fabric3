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
package org.fabric3.fabric.component.scope;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;
import org.osoa.sca.Conversation;

import org.fabric3.spi.component.InstanceWrapperStore;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.host.monitor.MonitorFactory;

/**
 * Scope container for the standard CONVERSATIONAL scope.
 *
 * @version $Rev$ $Date$
 */
@Service(ScopeContainer.class)
@EagerInit
public class ConversationalScopeContainer extends StatefulScopeContainer<Conversation> {
    public ConversationalScopeContainer(
            @Reference MonitorFactory monitorFactory,
            @Reference(name = "store")InstanceWrapperStore<Conversation> store) {
        super(Scope.CONVERSATION, monitorFactory.getMonitor(ScopeContainerMonitor.class), store);
    }
}
