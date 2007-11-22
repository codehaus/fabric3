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
package org.fabric3.fabric.domain;

import java.net.URI;
import java.util.Collection;

import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.spi.assembly.AssemblyStore;
import org.fabric3.spi.assembly.RecordException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.util.UriHelper;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class DomainServiceImpl implements DomainService {
    
    private LogicalComponent<CompositeImplementation> domain;

    private final AssemblyStore assemblyStore;
    
    public DomainServiceImpl(@Reference AssemblyStore assemblyStore) {
        this.assemblyStore = assemblyStore;
    }
    
    public void store() throws RecordException {
        assemblyStore.store(domain);
    }

    public LogicalComponent<?> findComponent(URI uri) {
        
        String defragmentedUri = UriHelper.getDefragmentedNameAsString(uri);
        String domainString = domain.getUri().toString();
        String[] hierarchy = defragmentedUri.substring(domainString.length() + 1).split("/");
        String currentUri = domainString;
        LogicalComponent<?> currentComponent = domain;
        for (String name : hierarchy) {
            currentUri = currentUri + "/" + name;
            currentComponent = currentComponent.getComponent(URI.create(currentUri));
            if (currentComponent == null) {
                return null;
            }
        }
        return currentComponent;
        
    }
    
    public Collection<LogicalComponent<?>> getComponents() {
        return domain.getComponents();
    }

    public LogicalComponent<CompositeImplementation> getDomain() {
        return domain;
    }

    public void initialize() throws AssemblyException {
        domain = assemblyStore.read();
    }

}
