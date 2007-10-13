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

import java.util.Properties;

import javax.transaction.TransactionManager;

import org.hibernate.HibernateException;
import org.hibernate.ejb.InjectionSettingsFactory;
import org.hibernate.transaction.TransactionManagerLookup;

/**
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class F3SettingsFactory extends InjectionSettingsFactory {
    
    private TransactionManagerLookup lookup;
    
    /**
     * Initializes the transaction manager lookup.
     * 
     * @param transactionManager Transaction manager.
     */
    public F3SettingsFactory(final TransactionManager transactionManager) {
        
        lookup = new TransactionManagerLookup() {

            public TransactionManager getTransactionManager(Properties props) throws HibernateException {
                return transactionManager;
            }

            public String getUserTransactionName() {
                return null;
            }
            
        };
        
    }

    /**
     * @see org.hibernate.cfg.SettingsFactory#createTransactionManagerLookup(java.util.Properties)
     */
    @Override
    protected TransactionManagerLookup createTransactionManagerLookup(Properties properties) {
        return lookup;
    }

}
