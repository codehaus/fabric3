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
package tx;

import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.TransactionManager;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class TransactionalServiceImpl implements TransactionalService {
    
    private String txmName;
    private TransactionManager txm;
    
    @Property(required = true)
    public void setTxmName(String txmName) {
        this.txmName = txmName;
    }
    
    @Init
    public void init() throws NamingException, SQLException {
        
        Context ic = null;
        try {
            ic = new InitialContext();
            txm = (TransactionManager) ic.lookup(txmName);
        } finally {
            if(ic != null) {
                ic.close();
            }
        }
        
    }

    public boolean isTransactionStarted() throws Exception {
        return txm.getStatus() == Status.STATUS_ACTIVE;
    }

}
