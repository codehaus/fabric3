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

package org.fabric3.pojo.instancefactory;

import java.util.LinkedList;
import java.util.List;

import org.fabric3.scdl.InjectionSiteMapping;
import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.Signature;
import org.fabric3.scdl.ValueSource;

/**
 * Base class for instance factory definitions.
 *
 * @version $Revsion$ $Date$
 */
public class InstanceFactoryDefinition extends ModelObject {
    private String implementationClass;
    private Signature constructor;
    private Signature initMethod;
    private Signature destroyMethod;
    private List<ValueSource> cdiSources = new LinkedList<ValueSource>();
    private List<InjectionSiteMapping> injectionSites = new LinkedList<InjectionSiteMapping>();

    /**
     * Returns the signature of the constrctor that should be used.
     * @return the signature of the constrctor that should be used
     */
    public Signature getConstructor() {
        return constructor;
    }

    /**
     * Sets the signature of the constrctor that should be used.
     * @param constructor the signature of the constrctor that should be used
     */
    public void setConstructor(Signature constructor) {
        this.constructor = constructor;
    }

    /**
     * Returns constructor injection names.
     *
     * @return the constructorNames Constructor injection names.
     */
    public List<ValueSource> getCdiSources() {
        return cdiSources;
    }

    /**
     * Adds a constructor injection name.
     *
     * @param cdiSource Constructor injection name.
     */
    public void addCdiSource(ValueSource cdiSource) {
        cdiSources.add(cdiSource);
    }

    /**
     * Gets the init method.
     *
     * @return the signature for the init method
     */
    public Signature getInitMethod() {
        return initMethod;
    }

    /**
     * Sets the init method.
     *
     * @param initMethod the signature of the init method
     */
    public void setInitMethod(Signature initMethod) {
        this.initMethod = initMethod;
    }

    /**
     * Gets the destroy method.
     *
     * @return the signature of the destroy method
     */
    public Signature getDestroyMethod() {
        return destroyMethod;
    }

    /**
     * Sets the destroy method.
     *
     * @param destroyMethod the signature of the destroy method
     */
    public void setDestroyMethod(Signature destroyMethod) {
        this.destroyMethod = destroyMethod;
    }

    /**
     * Gets the implementation class.
     *
     * @return Implementation class.
     */
    public String getImplementationClass() {
        return implementationClass;
    }

    /**
     * Sets the implementation class.
     *
     * @param implementationClass Implementation class.
     */
    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    /**
     * Gets the injection sites.
     *
     * @return Injection sites.
     */
    public List<InjectionSiteMapping> getInjectionSites() {
        return injectionSites;
    }

    /**
     * Adds an injection site.
     *
     * @param injectionSite site.
     */
    public void addInjectionSite(InjectionSiteMapping injectionSite) {
        injectionSites.add(injectionSite);
    }
}
