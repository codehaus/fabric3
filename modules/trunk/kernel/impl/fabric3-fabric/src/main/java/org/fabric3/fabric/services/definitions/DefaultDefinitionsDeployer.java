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
package org.fabric3.fabric.services.definitions;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;

import org.fabric3.scdl.definitions.AbstractDefinition;
import org.fabric3.scdl.definitions.BindingType;
import org.fabric3.scdl.definitions.ImplementationType;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionStoreRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.definitions.DefinitionActivationException;
import org.fabric3.spi.services.definitions.DefinitionsDeployer;
import org.fabric3.spi.services.definitions.DefinitionsRegistry;
import org.osoa.sca.annotations.Reference;

/**
 * Default implemenation of the definitions deployer.
 * 
 * @version $Revision$ $Date$
 */
public class DefaultDefinitionsDeployer implements DefinitionsDeployer {

    // Contribution store registry
    private ContributionStoreRegistry storeRegistry;

    // Definitions registry
    private DefinitionsRegistry definitionsRegistry;
    
    // URI prefix
    private String uriPrefix = "file://contribution/";

    /**
     * Injects the metadata store and definitions registry.
     * 
     * @param metaDataStore Injected metadata store.
     * @param definitionsRegistry Injected definitions registry.
     */
    public DefaultDefinitionsDeployer(@Reference ContributionStoreRegistry storeRegistry, @Reference DefinitionsRegistry definitionsRegistry) {
        this.storeRegistry = storeRegistry;
        this.definitionsRegistry = definitionsRegistry;
    }

    /**
     * @see org.fabric3.spi.services.definitions.DefinitionsDeployer#activateDefinition(javax.xml.namespace.QName)
     */
    public void activateDefinition(QName definitionName) throws DefinitionActivationException {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * @see org.fabric3.spi.services.definitions.DefinitionsDeployer#activateDefinitions(java.util.List)
     */
    public void activateDefinitions(List<URI> contributionUris) throws DefinitionActivationException {

        for (URI uri : contributionUris) {
            
            String storeId = parseStoreId(uri);
            MetaDataStore metaDataStore = storeRegistry.getMetadataStore(storeId);
            Contribution contribution = metaDataStore.find(uri);

            for (Resource resource : contribution.getResources()) {
                for (ResourceElement<?, ?> resourceElement : resource.getResourceElements()) {

                    if (!(resourceElement.getValue() instanceof AbstractDefinition)) {
                        continue;
                    }
                    activate(resourceElement);
                }
            }
        }
        
    }
    
    /*
     * Activates the definition.
     */
    private void activate(ResourceElement<?, ?> resourceElement) throws DefinitionActivationException {

        Object definition = resourceElement.getValue();
        
        if (definition instanceof Intent) {
            definitionsRegistry.registerDefinition((Intent) definition, Intent.class);
        } else if (definition instanceof PolicySet) {
            definitionsRegistry.registerDefinition((PolicySet) definition, PolicySet.class);
        } else if (definition instanceof BindingType) {
            definitionsRegistry.registerDefinition((BindingType) definition, BindingType.class);
        } else if (definition instanceof ImplementationType) {
            definitionsRegistry.registerDefinition((ImplementationType) definition, ImplementationType.class);
        } else {
            throw new DefinitionActivationException("Resource element not a definition", definition.getClass().getName());
        }
        
    }
    
    /*
     * Parse the store id.
     */
    private String parseStoreId(URI uri) {
        
        String s = uri.toString();
        assert s.length() > uriPrefix.length();
        s = s.substring(uriPrefix.length());
        int index = s.indexOf("/");
        if (index > 0) {
            return s.substring(0, index);
        } else {
            return s;
        }

    }

}
