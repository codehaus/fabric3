/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.pojo.reflection;

import java.lang.reflect.Field;

import org.fabric3.pojo.injection.MultiplicityObjectFactory;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;

/**
 * Injects a value created by an {@link org.fabric3.spi.ObjectFactory} on a given field
 *
 * @version $Rev$ $Date$
 */
public class FieldInjector<T> implements Injector<T> {

    private final Field field;

    private ObjectFactory<?> objectFactory;

    /**
     * Create an injector and have it use the given <code>ObjectFactory</code> to inject a value on the instance using the reflected
     * <code>Field</code>
     *
     * @param field         target field to inject on
     * @param objectFactory the object factor that creates the value to inject
     */
    public FieldInjector(Field field, ObjectFactory<?> objectFactory) {
        this.field = field;
        this.field.setAccessible(true);
        this.objectFactory = objectFactory;
    }

    /**
     * Inject a new value on the given isntance
     */
    public void inject(T instance) throws ObjectCreationException {
        try {
            Object o = objectFactory.getInstance();
            if (o == null) {
                // The object factory is "empty", e.g. a reference has not been wired yet. Avoid injecting onto the instance.
                // Note this is a correct assumption as there is no mechanism for configuring null values in SCA
                return;
            }
            field.set(instance, o);
        } catch (IllegalAccessException e) {
            String id = field.getName();
            throw new AssertionError("Field is not accessible:" + id);
        }
    }

    public void setObjectFactory(ObjectFactory<?> objectFactory, Object key) {
        
        if (this.objectFactory instanceof MultiplicityObjectFactory<?>) {
            ((MultiplicityObjectFactory<?>) this.objectFactory).addObjectFactory(objectFactory, key);
        } else {
            this.objectFactory = objectFactory;
        }
        
    }
}
