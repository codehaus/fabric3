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

import org.fabric3.extension.interceptor.InterceptorBuilderExtension;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.tx.proxy.TransactionManagerProxy;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class TxInterceptorBuilder extends InterceptorBuilderExtension<TxInterceptorDefinition, TxInterceptor> {

    // Transaction manager
    private TransactionManagerProxy transactionManager;
    
    /**
     * Injects the transaction manager.
     * 
     * @param transactionManager Injected transaction manager.
     */
    @Reference(required = true)
    public void setTransactionManager(TransactionManagerProxy transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    /**
     * @see org.fabric3.extension.interceptor.InterceptorBuilderExtension#getInterceptorDefinitionClass()
     */
    @Override
    protected Class<TxInterceptorDefinition> getInterceptorDefinitionClass() {
        return TxInterceptorDefinition.class;
    }

    /**
     * @see org.fabric3.spi.builder.interceptor.InterceptorBuilder#build(org.fabric3.spi.model.physical.PhysicalInterceptorDefinition)
     */
    public TxInterceptor build(TxInterceptorDefinition interceptorDefinition) throws BuilderException {
        return new TxInterceptor(transactionManager, interceptorDefinition.getAction());
    }


}
