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
package org.fabric3.fabric.services.domain;

import java.net.URI;
import java.util.Collection;

import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.runtime.assembly.LogicalComponentManager;
import org.fabric3.spi.runtime.assembly.LogicalComponentStore;
import org.fabric3.spi.runtime.assembly.RecordException;
import org.fabric3.spi.util.UriHelper;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class LogicalComponentManagerImpl implements LogicalComponentManager {
    
    private LogicalCompositeComponent domain;
    private final LogicalComponentStore logicalComponentStore;
    
    public LogicalComponentManagerImpl(@Reference LogicalComponentStore logicalComponentStore) {
        this.logicalComponentStore = logicalComponentStore;
    }

    public LogicalComponent<?> getComponent(URI uri) {
        
        String defragmentedUri = UriHelper.getDefragmentedNameAsString(uri);
        String domainString = domain.getUri().toString();
        String[] hierarchy = defragmentedUri.substring(domainString.length() + 1).split("/");
        String currentUri = domainString;
        LogicalComponent<?> currentComponent = domain;
        for (String name : hierarchy) {
            currentUri = currentUri + "/" + name;
            if (currentComponent instanceof LogicalCompositeComponent) {
                LogicalCompositeComponent composite = (LogicalCompositeComponent) currentComponent;
                currentComponent = composite.getComponent(URI.create(currentUri));
            }
            if (currentComponent == null) {
                return null;
            }
        }
        return currentComponent;
        
    }
    
    public Collection<LogicalComponent<?>> getComponents() {
        return domain.getComponents();
    }

    public LogicalCompositeComponent getDomain() {
        return domain;
    }

    public void initialize() throws AssemblyException {
        domain = (LogicalCompositeComponent) logicalComponentStore.read();
    }
    
    public void store() throws RecordException {
        logicalComponentStore.store(domain);
    }

}
