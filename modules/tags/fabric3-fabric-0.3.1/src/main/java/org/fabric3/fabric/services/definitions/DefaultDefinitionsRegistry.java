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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;

import org.fabric3.scdl.definitions.AbstractDefinition;
import org.fabric3.scdl.definitions.BindingType;
import org.fabric3.scdl.definitions.ImplementationType;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.services.definitions.DefinitionsRegistry;

/**
 * Default implementation of the definitions registry.
 * 
 * @version $Revision$ $Date$
 */
public class DefaultDefinitionsRegistry implements DefinitionsRegistry {
    
    // Definition cache
    private Map<Class<? extends AbstractDefinition>, Map<QName, ? extends AbstractDefinition>> cache = 
        new ConcurrentHashMap<Class<? extends AbstractDefinition>, Map<QName,? extends AbstractDefinition>>();
    
    /**
     * Initializes the cache.
     */
    public DefaultDefinitionsRegistry() {
        
        cache.put(Intent.class, new ConcurrentHashMap<QName, Intent>());
        cache.put(PolicySet.class, new ConcurrentHashMap<QName, PolicySet>());
        cache.put(BindingType.class, new ConcurrentHashMap<QName, BindingType>());
        cache.put(ImplementationType.class, new ConcurrentHashMap<QName, ImplementationType>());
        
    }

    @SuppressWarnings("unchecked")
    public <D extends AbstractDefinition> Set<D> getAllDefinitions(Class<D> definitionClass) {
        
        Map<QName, D> subCache = (Map<QName, D>) cache.get(definitionClass);
        
        Set<D> definitions = new HashSet<D>();
        definitions.addAll(subCache.values());
        
        return definitions;
        
    }

    @SuppressWarnings("unchecked")
    public <D extends AbstractDefinition> D getDefinition(QName name, Class<D> definitionClass) {
        
        Map<QName, D> subCache = (Map<QName, D>) cache.get(definitionClass);
        return subCache.get(name);
        
    }

    @SuppressWarnings("unchecked")
    public <D extends AbstractDefinition> void registerDefinition(D definition, Class<D> definitionClass) {
        
        Map<QName, D> subCache = (Map<QName, D>) cache.get(definitionClass);
        subCache.put(definition.getName(), definition);
        
        subCache.put(definition.getName(), definition);

    }

}
