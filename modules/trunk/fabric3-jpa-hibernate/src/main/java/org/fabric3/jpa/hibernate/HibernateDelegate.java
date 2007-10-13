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
package org.fabric3.jpa.hibernate;

import java.lang.reflect.Field;
import java.util.Collections;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.transaction.TransactionManager;

import org.fabric3.jpa.spi.delegate.EmfBuilderDelegate;
import org.hibernate.cfg.SettingsFactory;
import org.hibernate.ejb.Ejb3Configuration;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class HibernateDelegate implements EmfBuilderDelegate {
    
    private TransactionManager transactionManager;
    private SettingsFactory settingsFactory;
    
    /**
     * Injects the transaction manager.
     * 
     * @param transactionManager Transaction manager.
     */
    @Reference
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    /**
     * Creates the setting factor.
     */
    @Init
    public void start() {
        settingsFactory = new F3SettingsFactory(transactionManager);
    }

    /**
     * @see org.fabric3.jpa.spi.delegate.EmfBuilderDelegate#build(javax.persistence.spi.PersistenceUnitInfo, 
     *                                                            java.lang.ClassLoader)
     */
    public EntityManagerFactory build(PersistenceUnitInfo info, ClassLoader classLoader) {
        
        Ejb3Configuration cfg = new Ejb3Configuration();
        
        // TODO this is an evil hack, request feature addition to hibernate to have overridable settings factory
        try {
            Field field = Ejb3Configuration.class.getDeclaredField("settingsFactory");
            field.setAccessible(true);
            field.set(cfg, settingsFactory);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
        cfg.configure(info, Collections.emptyMap());
        
        return cfg.buildEntityManagerFactory();
    }

}
