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
package org.fabric3.web.runtime;

import java.util.List;
import java.util.Map;

import org.fabric3.pojo.reflection.Injector;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.scdl.InjectionSite;

/**
 * Creates Injector collections for injecting references, properties and context proxies into web application artifacts. These include servlets,
 * filters, the servlet context, and the session context.
 *
 * @version $Revision$ $Date$
 */
public interface InjectorFactory {
    /**
     * Populates a map of Injectors for each injectable artifact (servlet, filter, servlet context or session context) in the  web application.
     *
     * @param injectors    the map to populate, keyed by artifact id (e.g. servlet class name)
     * @param siteMappings a map keyed by site name (e.g. a reference or property name). The value is a map keyed by injectable artifact id with a
     *                     value containing a description of the injection site. For example, a reference may be injected on fields of multiple
     *                     servlets. This would be represented by an entry keyed on reference name with a value of a map keyed by servlet class name
     *                     and values containing injection site descriptions of the servlet fields.
     * @param factories    the object factories that supply injected values.
     * @param classLoader  the classloader to load classes in for the web application
     * @throws InjectionCreationException if an error occurs creating the injectors.
     */
    void createInjectorMappings(Map<String, List<Injector<?>>> injectors,
                                Map<String, Map<String, InjectionSite>> siteMappings,
                                Map<String, ObjectFactory<?>> factories,
                                ClassLoader classLoader) throws InjectionCreationException;
}
