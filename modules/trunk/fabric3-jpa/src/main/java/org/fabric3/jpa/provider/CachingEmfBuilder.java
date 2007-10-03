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
package org.fabric3.jpa.provider;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.osoa.sca.annotations.Destroy;

/**
 * Creates entity manager factories using the JPA provider SPI. Creation of entity manager 
 * factories are expensive operations and hence created instances are cached.
 * 
 * @version $Revision$ $Date$
 */
public class CachingEmfBuilder implements EmfBuilder {
    
    private Map<String, EntityManagerFactory> cache = new HashMap<String, EntityManagerFactory>();

    /**
     * @see org.fabric3.jpa.provider.EmfBuilder#build(java.lang.String, java.lang.ClassLoader)
     */
    public synchronized EntityManagerFactory build(String unitName, ClassLoader classLoader) {
        
        if(cache.containsKey(unitName)) {
            return cache.get(unitName);
        }
        
        EntityManagerFactory emf = createEntityManagerFactory(unitName);
        cache.put(unitName, emf);
        
        return emf;
        
    }
    
    /**
     * Closes the entity manager factories.
     */
    @Destroy
    public void destroy() {
        for (EntityManagerFactory emf : cache.values()) {
            emf.close();
        }
    }

    /*
     * Creates the entity manager factory using the JPA provider API.
     */
    private EntityManagerFactory createEntityManagerFactory(String unitName) {
        // TODO Auto-generated method stub
        return null;
    }

}
