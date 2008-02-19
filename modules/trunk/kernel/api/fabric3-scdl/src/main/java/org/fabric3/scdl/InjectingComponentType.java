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

import java.util.HashMap;
import java.util.Map;

/**
 * A component type associated with an implementation that supports injection.
 *
 * @version $Rev$ $Date$
 */
public class InjectingComponentType extends AbstractComponentType<ServiceDefinition, ReferenceDefinition, Property<?>, ResourceDefinition> {

    private final Map<ValueSource, InjectionSite> injectionMappings = new HashMap<ValueSource, InjectionSite>();
    private Signature initMethod;
    private Signature destroyMethod;
    private final Map<String, CallbackDefinition> callbacks = new HashMap<String, CallbackDefinition>();

    /**
     * Default constructor.
     */
    public InjectingComponentType() {
    }

    /**
     * Add a reference and associate with an injection site.
     *
     * @param reference     the reference to add
     * @param injectionSite the injection site for the reference
     */
    public void add(ReferenceDefinition reference, InjectionSite injectionSite) {
        super.add(reference);
        ValueSource valueSource = new ValueSource(ValueSource.ValueSourceType.REFERENCE, reference.getName());
        addInjectionSite(valueSource, injectionSite);
    }

    /**
     * Add a property and associate with an injection site.
     *
     * @param property      the property to add
     * @param injectionSite the injection site for the property
     */
    public void add(Property<?> property, InjectionSite injectionSite) {
        super.add(property);
        ValueSource valueSource = new ValueSource(ValueSource.ValueSourceType.PROPERTY, property.getName());
        addInjectionSite(valueSource, injectionSite);
    }

    /**
     * Add a resource and associate with an injection site.
     *
     * @param resource      the resource to add
     * @param injectionSite the injection site for the resource
     */
    public void add(ResourceDefinition resource, InjectionSite injectionSite) {
        super.add(resource);
        ValueSource valueSource = new ValueSource(ValueSource.ValueSourceType.RESOURCE, resource.getName());
        addInjectionSite(valueSource, injectionSite);
    }

    /**
     * Adds a callback proxy defintion and its associated injection site
     *
     * @param definition    the callback proxy definition
     * @param injectionSite the proxy injection site
     */
    public void add(CallbackDefinition definition, InjectionSite injectionSite) {
        String name = definition.getName();
        callbacks.put(name, definition);
        ValueSource valueSource = new ValueSource(ValueSource.ValueSourceType.CALLBACK, name);
        injectionMappings.put(valueSource, injectionSite);
    }

    /**
     * Returns a collection of defined callback proxy definitions keyed by name
     *
     * @return the collection of proxy definitions
     */
    public Map<String, CallbackDefinition> getCallbacks() {
        return callbacks;
    }

    /**
     * Add the injection site for an injectable value.
     *
     * @param source the value to be injected
     * @param site   the injection site
     */
    public void addInjectionSite(ValueSource source, InjectionSite site) {
        injectionMappings.put(source, site);
    }

    /**
     * Returns the injection site for a value.
     *
     * @param source the value for which the site should be returned
     * @return in the injection site for the supplied value
     */
    public InjectionSite getInjectionSite(ValueSource source) {
        return injectionMappings.get(source);
    }

    /**
     * Returns the map of all injection mappings.
     *
     * @return the map of all injection mappings
     */
    public Map<ValueSource, InjectionSite> getInjectionMappings() {
        return injectionMappings;
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
