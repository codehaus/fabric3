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
package org.fabric3.web.introspection;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;

/**
 * Introspects the contents of a web application for a web component and generates a corresponding component type. Introspected information will be
 * derive from implementation classes and the web.xml deployment descriptor.
 *
 * @version $Revision$ $Date$
 */
public interface WebImplementationIntrospector {
    /**
     * Introspect the web application.
     *
     * @param implementation the web component implementation
     * @param context        the current introspection context
     * @throws IntrospectionException if an error is encountered during introspection
     */
    void introspect(WebImplementation implementation, IntrospectionContext context) throws IntrospectionException;

}
