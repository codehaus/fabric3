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

import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;

/**
 * A map based object factory.
 *
 * @version $Rev: 1 $ $Date: 2007-05-14 18:40:37 +0100 (Mon, 14 May 2007) $
 */
public class MapMultiplicityObjectFactory implements MultiplicityObjectFactory<Map<?, ?>> {

    // Object factories
    private Map<Object, ObjectFactory<?>> factories = new HashMap<Object, ObjectFactory<?>>();

    /**
     * @see org.fabric3.spi.ObjectFactory#getInstance()
     */
    public Map<Object, Object> getInstance() throws ObjectCreationException {
        Map<Object, Object> map = new HashMap<Object, Object>();
        for (Map.Entry<Object, ObjectFactory<?>> entry : factories.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getInstance());
        }
        return map;
    }

    /**
     * @see org.fabric3.pojo.injection.MultiplicityObjectFactory#addObjectFactory(org.fabric3.spi.ObjectFactory, org.fabric3.spi.component.AtomicComponent)
     */
    public void addObjectFactory(ObjectFactory<?> objectFactory, Object key) {
        factories.put(key, objectFactory);
    }

}
