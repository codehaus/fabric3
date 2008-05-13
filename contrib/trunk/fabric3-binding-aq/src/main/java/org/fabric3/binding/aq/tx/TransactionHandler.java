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
package org.fabric3.binding.aq.tx;

import javax.jms.Session;


/**
 * @version $Revision$ $Date$
 */
public interface TransactionHandler {
    
    
    /**
     * Start the global transaction
     * @throws AQJmsTxException
     */
    void enlist(Session session) throws AQJmsTxException;
    
    /**
     * Commits global transaction
     * @throws AQJmsTxException
     */
    void commit() throws AQJmsTxException;
    
    /**
     * Rollback's the global transaction 
     * @throws AQJmsTxException
     */
    void rollback() throws AQJmsTxException;
}
