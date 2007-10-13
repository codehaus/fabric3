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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;

import org.fabric3.jpa.Fabric3JpaException;
import org.fabric3.jpa.spi.delegate.EmfBuilderDelegate;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Reference;

/**
 * Creates entity manager factories using the JPA provider SPI. Creation of entity manager 
 * factories are expensive operations and hence created instances are cached.
 * 
 * @version $Revision$ $Date$
 */
public class CachingEmfBuilder implements EmfBuilder {
    
    private Map<String, EntityManagerFactory> cache = new HashMap<String, EntityManagerFactory>();
    private PersistenceUnitScanner scanner;
    private Map<String, EmfBuilderDelegate> delegates = new HashMap<String, EmfBuilderDelegate>();
    
    /**
     * Injects the scanner.
     * 
     * @param scanner Injected scanner.
     */
    public CachingEmfBuilder(@Reference PersistenceUnitScanner scanner) {
        this.scanner = scanner;
    }
    
    /**
     * Injects the delegates.
     * 
     * @param delegates Provider specific delegates.
     */
    @Reference
    public void setDelegates(Map<String, EmfBuilderDelegate> delegates) {
        this.delegates = delegates;
    }

    /**
     * @see org.fabric3.jpa.provider.EmfBuilder#build(java.lang.String, java.lang.ClassLoader)
     */
    public synchronized EntityManagerFactory build(String unitName, ClassLoader classLoader) {
        
        if(cache.containsKey(unitName)) {
            return cache.get(unitName);
        }
        
        EntityManagerFactory emf = createEntityManagerFactory(unitName, classLoader);
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
    private EntityManagerFactory createEntityManagerFactory(String unitName, ClassLoader classLoader) {
        
        PersistenceUnitInfo info = scanner.getPersistenceUnitInfo(unitName, classLoader);
        String providerClass = info.getPersistenceProviderClassName();
        
        EmfBuilderDelegate delegate = delegates.get(providerClass);
        if(delegate != null) {
            return delegate.build(info, classLoader);
        }

        // No configured delegates, try standard JPA
        try {
            PersistenceProvider provider = (PersistenceProvider) Class.forName(providerClass).newInstance();
            return provider.createContainerEntityManagerFactory(info, Collections.emptyMap());
        } catch (InstantiationException ex) {
            throw new Fabric3JpaException(ex);
        } catch (IllegalAccessException ex) {
            throw new Fabric3JpaException(ex);
        } catch (ClassNotFoundException ex) {
            throw new Fabric3JpaException(ex);
        }

    }

}
