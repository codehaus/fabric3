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
package org.fabric3.jpa.runtime;

import javax.transaction.TransactionManager;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.ObjectCreationException;

/**
 * Creates MultiThreadedEntityManagerProxy instances.
 *
 * @version $Revision$ $Date$
 */
public class MultiThreadedEntityManagerProxyFactory implements ObjectFactory<MultiThreadedEntityManagerProxy> {
    private String unitName;
    private EntityManagerService service;
    private TransactionManager tm;
    private boolean extended;

    public MultiThreadedEntityManagerProxyFactory(String unitName, boolean extended, EntityManagerService service, TransactionManager tm) {
        this.service = service;
        this.tm = tm;
        this.extended = extended;
        this.unitName = unitName;
    }

    public MultiThreadedEntityManagerProxy getInstance() throws ObjectCreationException {
        return new MultiThreadedEntityManagerProxy(unitName, extended, service, tm);
    }
}