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

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.transaction.TransactionManager;

import org.fabric3.jpa.spi.delegate.EmfBuilderDelegate;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class HibernateDelegate implements EmfBuilderDelegate {
    
    private TransactionManager transactionManager;
    
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
     * @see org.fabric3.jpa.spi.delegate.EmfBuilderDelegate#build(javax.persistence.spi.PersistenceUnitInfo, 
     *                                                            java.lang.ClassLoader)
     */
    public EntityManagerFactory build(PersistenceUnitInfo info, ClassLoader classLoader) {
        
        assert transactionManager != null;
        // TODO Auto-generated method stub
        return null;
    }

}
