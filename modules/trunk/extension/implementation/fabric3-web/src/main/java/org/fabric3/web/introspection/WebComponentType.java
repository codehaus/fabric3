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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.scdl.ComponentType;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.InjectableAttribute;

/**
 * A component type representing a web component.
 *
 * @version $Revision$ $Date$
 */
public class WebComponentType extends ComponentType {
    private final Map<String, Map<InjectionSite, InjectableAttribute>> sites = new HashMap<String, Map<InjectionSite, InjectableAttribute>>();

    /**
     * Returns a mapping from artifact id (e.g. servlet or filter class name, servlet context, session context) to injection site/injectable attribute
     * pair
     *
     * @return the mapping
     */
    public Map<String, Map<InjectionSite, InjectableAttribute>> getInjectionSites() {
        return Collections.unmodifiableMap(sites);
    }

    /**
     * Sets a mapping from artifact id to injection site/injectable attribute pair.
     *
     * @param artifactId the artifact id
     * @param site       the injeciton site
     * @param attribute  the injectable attribute
     */
    public void addMapping(String artifactId, InjectionSite site, InjectableAttribute attribute) {
        Map<InjectionSite, InjectableAttribute> mapping = sites.get(artifactId);
        if (mapping == null) {
            mapping = new HashMap<InjectionSite, InjectableAttribute>();
            sites.put(artifactId, mapping);
        }
        mapping.put(site, attribute);
    }
}
