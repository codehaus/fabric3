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

import javax.transaction.TransactionManager;

import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.builder.interceptor.InterceptorBuilderRegistry;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class TxInterceptorBuilder implements InterceptorBuilder<TxInterceptorDefinition, TxInterceptor> {
    private InterceptorBuilderRegistry registry;
    // Transaction manager
    private TransactionManager transactionManager;

    public TxInterceptorBuilder(@Reference InterceptorBuilderRegistry registry,
                                @Reference TransactionManager transactionManager) {
        this.registry = registry;
        this.transactionManager = transactionManager;
    }

    @Init
    public void init() {
        registry.register(TxInterceptorDefinition.class, this);
    }

    public TxInterceptor build(TxInterceptorDefinition interceptorDefinition) throws BuilderException {
        return new TxInterceptor(transactionManager, interceptorDefinition.getAction());
    }


}
