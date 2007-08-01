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
package org.fabric3.fabric.injection;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;

/**
 * A map based object factory.
 *
 * @version $Rev: 1 $ $Date: 2007-05-14 18:40:37 +0100 (Mon, 14 May 2007) $
 */
public class MapMultiplicityObjectFactory<K, V> implements ObjectFactory<Map<K, V>> {

    // Object factories
    private Map<K, ObjectFactory<V>> factories = new HashMap<K, ObjectFactory<V>>();
    
    /**
     * Adds an object factory.
     * @param objectFactory Object factory to be used.
     */
    public void addObjectFactory(K key, ObjectFactory<V> objectFactory) {
        factories.put(key, objectFactory);
    }

    /**
     * @see org.fabric3.spi.ObjectFactory#getInstance()
     */
    public Map<K, V> getInstance() throws ObjectCreationException {
        Map<K, V> map = new HashMap<K, V>();
        for (Map.Entry<K, ObjectFactory<V>> entry : factories.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getInstance());
        }
        return map;
    }

}
