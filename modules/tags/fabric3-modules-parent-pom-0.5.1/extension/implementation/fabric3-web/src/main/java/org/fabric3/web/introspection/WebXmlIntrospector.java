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

import java.util.List;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;

/**
 * Introspects a web.xml descriptor.
 *
 * @version $Revision$ $Date$
 */
public interface WebXmlIntrospector {

    /**
     * Returns the loaded classes for servlets, filters, and listeners configured in the web.xml. Errors will be collated in the
     * IntrospectionContext.
     *
     * @param context the introspection context. Classes will be loaded in the target classloader associated with the context.
     * @return the collection of loaded classes
     */
    List<Class<?>> introspectArtifactClasses(IntrospectionContext context);

}
