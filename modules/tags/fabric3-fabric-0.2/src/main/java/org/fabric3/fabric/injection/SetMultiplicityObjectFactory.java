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

import java.util.HashSet;
import java.util.Set;

import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.AtomicComponent;

/**
 * A set based object factory.
 *
 * @version $Rev: 1 $ $Date: 2007-05-14 18:40:37 +0100 (Mon, 14 May 2007) $
 */
public class SetMultiplicityObjectFactory implements MultiplicityObjectFactory<Set<?>> {

    // Object factories
    private Set<ObjectFactory<?>> factories = new HashSet<ObjectFactory<?>>();

    /**
     * @see org.fabric3.spi.ObjectFactory#getInstance()
     */
    public Set<Object> getInstance() throws ObjectCreationException {
        Set<Object> set = new HashSet<Object>();
        for (ObjectFactory<?> factory : factories) {
            set.add(factory.getInstance());
        }
        return set;
    }

    /**
     * @see org.fabric3.fabric.injection.MultiplicityObjectFactory#addObjectFactory(org.fabric3.spi.ObjectFactory, org.fabric3.spi.component.AtomicComponent)
     */
    public void addObjectFactory(ObjectFactory<?> objectFactory, AtomicComponent<?> targetComponent) {
        factories.add(objectFactory);
    }

}
