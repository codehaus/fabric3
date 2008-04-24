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
package loanapp.store.memory;

import loanapp.store.StoreService;
import loanapp.store.StoreException;
import loanapp.message.LoanApplication;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osoa.sca.annotations.Scope;

/**
 * Simple in-memory StoreService that uses a Map for persistence.
 *
 * @version $Revision$ $Date$
 */
@Scope("COMPOSITE")
public class MemoryStoreComponent implements StoreService {
    private Map<String, LoanApplication> cache = new ConcurrentHashMap<String, LoanApplication>();

    public void save(LoanApplication application) throws StoreException {
        cache.put(application.getId(), application);
    }

    public void update(LoanApplication application) throws StoreException {
        save(application);
    }

    public void remove(String id) throws StoreException {
        cache.remove(id);
    }

    public LoanApplication find(String id) throws StoreException {
        return cache.get(id);
    }
}
