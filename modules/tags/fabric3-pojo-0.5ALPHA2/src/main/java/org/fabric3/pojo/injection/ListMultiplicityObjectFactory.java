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
package org.fabric3.pojo.injection;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;

/**
 * Resolves targets configured in a multiplicity by delegating to object factories and returning an <code>List</code>
 * containing object instances
 *
 * @version $Rev$ $Date$
 */
public class ListMultiplicityObjectFactory implements MultiplicityObjectFactory<List<?>> {

    // Object factories
    private List<ObjectFactory<?>> factories = new CopyOnWriteArrayList<ObjectFactory<?>>();

    public List<Object> getInstance() throws ObjectCreationException {
        List<Object> list = new CopyOnWriteArrayList<Object>();
        for (ObjectFactory<?> factory : factories) {
            list.add(factory.getInstance());
        }
        return list;
    }

    public void addObjectFactory(ObjectFactory<?> objectFactory, Object key) {
        factories.add(objectFactory);
    }

}
