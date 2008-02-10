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

    private final Map<ValueSource, MemberSite> injectionMappings = new HashMap<ValueSource, MemberSite>();
    private Signature initMethod;
    private Signature destroyMethod;

    /**
     * Default constructor.
     */
    public InjectingComponentType() {
    }

    /**
     * Add a reference and associate with an injection site.
     * @param reference the reference to add
     * @param memberSite the injection site for the reference
     */
    public void add(ReferenceDefinition reference, MemberSite memberSite) {
        super.add(reference);
        ValueSource valueSource = new ValueSource(ValueSource.ValueSourceType.REFERENCE, reference.getName());
        injectionMappings.put(valueSource, memberSite);
    }

    /**
     * Add a property and associate with an injection site.
     * @param property the property to add
     * @param memberSite the injection site for the property
     */
    public void add(Property<?> property, MemberSite memberSite) {
        super.add(property);
        ValueSource valueSource = new ValueSource(ValueSource.ValueSourceType.PROPERTY, property.getName());
        injectionMappings.put(valueSource, memberSite);
    }

    /**
     * Returns the component initializer method.
     *
     * @return the component initializer method
     */
    public Signature getInitMethod() {
        return initMethod;
    }

    /**
     * Sets the component initializer method.
     *
     * @param initMethod the component initializer method
     */
    public void setInitMethod(Signature initMethod) {
        this.initMethod = initMethod;
    }

    /**
     * Returns the component destructor method.
     *
     * @return the component destructor method
     */
    public Signature getDestroyMethod() {
        return destroyMethod;
    }

    /**
     * Sets the component destructor method.
     *
     * @param destroyMethod the component destructor method
     */
    public void setDestroyMethod(Signature destroyMethod) {
        this.destroyMethod = destroyMethod;
    }
}
