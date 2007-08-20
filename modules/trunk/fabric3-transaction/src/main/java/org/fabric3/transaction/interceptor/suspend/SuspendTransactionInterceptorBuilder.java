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
package org.fabric3.transaction.interceptor.suspend;

import javax.transaction.TransactionManager;

import org.fabric3.extension.interceptor.InterceptorBuilderExtension;
import org.fabric3.spi.builder.BuilderException;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class SuspendTransactionInterceptorBuilder extends InterceptorBuilderExtension<SuspendTransactionInterceptorDefinition, SuspendTransactionInterceptor> {

    // Transaction manager
    private TransactionManager transactionManager;
    
    /**
     * Injects the transaction manager.
     * 
     * @param transactionManager Injected transaction manager.
     */
    @Reference(required = true)
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    
    /**
     * @see org.fabric3.extension.interceptor.InterceptorBuilderExtension#getInterceptorDefinitionClass()
     */
    @Override
    protected Class<SuspendTransactionInterceptorDefinition> getInterceptorDefinitionClass() {
        return SuspendTransactionInterceptorDefinition.class;
    }

    /**
     * @see org.fabric3.spi.builder.interceptor.InterceptorBuilder#build(org.fabric3.spi.model.physical.PhysicalInterceptorDefinition)
     */
    public SuspendTransactionInterceptor build(SuspendTransactionInterceptorDefinition interceptorDefinition) throws BuilderException {
        return new SuspendTransactionInterceptor(transactionManager);
    }


}
