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
package org.fabric3.fabric.command;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import org.fabric3.spi.command.AbstractCommand;

/**
 * Starts a context with the composite container
 *
 * @version $Rev$ $Date$
 */
public class StartCompositeContextCommand extends AbstractCommand {

    private final Set<URI> groupIds = new LinkedHashSet<URI>();

    public StartCompositeContextCommand(int order) {
        super(order);
    }

    public Set<URI> getGroupIds() {
        return groupIds;
    }
    
    public void addGroupId(URI groupId) {
        groupIds.add(groupId);
    }
    
    public void addGroupIds(Set<URI> groupIds) {
        this.groupIds.addAll(groupIds);
    }
}
