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
package org.fabric3.introspection.impl.annotation;

import org.fabric3.host.contribution.ValidationFailure;

/**
 * Denotes an invalid accessor value for a field or method annotated with @Resource, @Reference, or @Property.
 *
 * @version $Revision$ $Date$
 */
public class InvalidAccessor extends ValidationFailure<Class<?>> {
    private String message;

    public InvalidAccessor(String message, Class<?> implClass) {
        super(implClass);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
