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
package org.fabric3.api.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Used to demarcate expected data types for an operation
 *
 * @version $Rev$ $Date$
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface DataType {

    /**
     * Returns the unique name of the data binding
     * @return the unique name of the data binding
     */
    String name();

    /**
     * Returns the logical data type
     * @return the logical data type
     */
    Class logicalType() default Object.class;

    /**
     * Returns the physical data type
     * @return the physical data type
     */
    Class physicalType() default Object.class;

    /**
     * Returns an array of extensibility elements
     * @return an array of extensibility elements
     */
    DataContext[] context() default {};

}
