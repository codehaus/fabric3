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
package org.fabric3.tx.interceptor;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;
import org.fabric3.tx.TxAction;
import org.fabric3.tx.TxException;

/**
 * @version $Revision$ $Date$
 */
public class TxInterceptor implements Interceptor {
    
    // Next interceptor
    private Interceptor next;
    
    // Transaction manager
    private TransactionManager transactionManager;
    
    // Transaction action
    private TxAction txAction;
    
    /**
     * Initializes the transaction manager.
     * 
     * @param transactionManager Transaction manager to be initialized.
     */
    public TxInterceptor(TransactionManager transactionManager, TxAction txAction) {
        this.transactionManager = transactionManager;
        this.txAction = txAction;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#getNext()
     */
    public Interceptor getNext() {
        return next;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#invoke(org.fabric3.spi.wire.Message)
     */
    public Message invoke(Message message) {
        
        Transaction transaction = null;
        try {

            transaction = transactionManager.getTransaction();
            
            if(txAction == TxAction.BEGIN && transaction == null) {
                transactionManager.begin();
            } else if(txAction == TxAction.SUSPEND && transaction != null) {
                transactionManager.suspend();
            }

            return next.invoke(message);
            
        } catch(Exception ex) {
            throw new TxException(ex);
        } finally {
            
            if(txAction == TxAction.BEGIN && transaction == null) {
                try {
                    transactionManager.commit();
                } catch(Exception ex) {
                    throw new TxException(ex);
                }
            } else if(txAction == TxAction.SUSPEND && transaction != null) {
                try {
                    transactionManager.resume(transaction);
                } catch(Exception ex) {
                    throw new TxException(ex);
                }
            }

        }
        
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#setNext(org.fabric3.spi.wire.Interceptor)
     */
    public void setNext(Interceptor next) {
        this.next = next;
    }

}
