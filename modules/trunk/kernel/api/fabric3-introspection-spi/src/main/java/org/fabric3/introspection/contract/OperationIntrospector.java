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
package org.fabric3.introspection.contract;

import java.lang.reflect.Method;

import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ValidationContext;

/**
 * Implementations evaluate the methods of a Java-based interface and populate the operation on the corresponding service contract with relevant
 * metadata.
 *
 * @version $Revision$ $Date$
 */
public interface OperationIntrospector {

    /**
     * Perform the introspection.
     *
     * @param operation the operation to update
     * @param method    the method to evaluate
     * @param context   the validation cotnext to report errors and warnings.
     */
    <T> void introspect(Operation<T> operation, Method method, ValidationContext context);

}
