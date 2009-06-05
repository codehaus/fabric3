/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.pojo.provision;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectionSite;
import org.fabric3.model.type.java.Signature;

/**
 * Base class for instance factory definitions.
 *
 * @version $Revsion$ $Date$
 */
public class InstanceFactoryDefinition implements Serializable {
    private static final long serialVersionUID = 3516497485593609161L;
    private String implementationClass;
    private Signature constructor;
    private Signature initMethod;
    private Signature destroyMethod;
    private boolean reinjectable;
    private Map<InjectionSite, InjectableAttribute> construction = new HashMap<InjectionSite, InjectableAttribute>();
    private Map<InjectionSite, InjectableAttribute> postConstruction = new HashMap<InjectionSite, InjectableAttribute>();
    private Map<InjectionSite, InjectableAttribute> reinjection = new HashMap<InjectionSite, InjectableAttribute>();

    /**
     * Returns the signature of the constrctor that should be used.
     *
     * @return the signature of the constrctor that should be used
     */
    public Signature getConstructor() {
        return constructor;
    }

    /**
     * Sets the signature of the constrctor that should be used.
     *
     * @param constructor the signature of the constrctor that should be used
     */
    public void setConstructor(Signature constructor) {
        this.constructor = constructor;
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
     * Returns the map of injections to be performed during construction.
     *
     * @return the map of injections to be performed during construction
     */
    public Map<InjectionSite, InjectableAttribute> getConstruction() {
        return construction;
    }

    /**
     * Returns the map of injections to be performed after construction.
     *
     * @return the map of injections to be performed after construction
     */
    public Map<InjectionSite, InjectableAttribute> getPostConstruction() {
        return postConstruction;
    }

    /**
     * Returns the map of injections to be performed during reinjection.
     *
     * @return the map of injections to be performed during reinjection
     */
    public Map<InjectionSite, InjectableAttribute> getReinjection() {
        return reinjection;
    }

    /**
     * Returns true if the implementation is reinjectable, e.g. it is composite-scoped.
     *
     * @return true if the implementation is reinjectable, e.g. it is composite-scoped
     */
    public boolean isReinjectable() {
        return reinjectable;
    }

    /**
     * sets if the implementation is reinjectable.
     *
     * @param reinjectable true if the implementation is reinjectable
     */
    public void setReinjectable(boolean reinjectable) {
        this.reinjectable = reinjectable;
    }
}
