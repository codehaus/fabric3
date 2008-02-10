/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.scdl;

import java.util.Map;
import java.util.HashMap;

/**
 * A component type associated with an implementation that supports injection.
 *
 * @version $Rev$ $Date$
 */
public class InjectingComponentType extends AbstractComponentType<ServiceDefinition, ReferenceDefinition, Property<?>, ResourceDefinition> {

    private final Map<ValueSource, InjectionSiteMapping> injectionMappings = new HashMap<ValueSource, InjectionSiteMapping>();

    /**
     * Default constructor.
     */
    public InjectingComponentType() {
    }


    /**
     * Add a mapping from a value source (property, reference etc.) to an injection site.
     *
     * @param valueSource the value that would be injected
     * @param memberSite the site where the value should be injected
     */
    public void addInjectionMapping(ValueSource valueSource, MemberSite memberSite) {
        InjectionSiteMapping mapping = new InjectionSiteMapping();
        mapping.setSource(valueSource);
        mapping.setSite(memberSite);
        injectionMappings.put(valueSource, mapping);
    }

    /**
     * Returns a live map of mappings for each value source.
     *
     * @return a live map of mappings for each value source
     */
    public Map<ValueSource, InjectionSiteMapping> getInjectionMappings() {
        return injectionMappings;
    }
}
