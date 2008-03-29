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

import java.util.LinkedHashSet;
import java.util.Set;

import org.fabric3.spi.command.AbstractCommand;

/**
 * A command to initialize a composite scoped component on a service node that is included in an activated composite.
 * For example, composite-scoped components included at the domain level.
 *
 * @version $Rev$ $Date$
 */
public class InitializeComponentCommand extends AbstractCommand {
    
    private final Set<ComponentInitializationUri> uris = new LinkedHashSet<ComponentInitializationUri>();
    

    public InitializeComponentCommand(int order) {
        super(order);
    }

    public Set<ComponentInitializationUri> getUris() {
        return uris;
    }
    
    public void addUri(ComponentInitializationUri uri) {
        uris.add(uri);
    }
    
    public void addUris(Set<ComponentInitializationUri> uris) {
        this.uris.addAll(uris);
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (obj == null || obj.getClass() != InitializeComponentCommand.class) {
            return false;
        }
        
        InitializeComponentCommand other = (InitializeComponentCommand) obj;
        return uris.equals(other.uris);
        
    }

    @Override
    public int hashCode() {
        return uris.hashCode();
    }
    
}
