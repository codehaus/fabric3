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
package org.fabric3.scdl.definitions;

import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * Represents an implementation type.
 * 
 * @version $Revision$ $Date$
 */
public class ImplementationType extends AbstractDefinition {

    private final Set<QName> alwaysProvide;
    private final Set<QName> mayProvide;
    
    /**
     * @param name Name of the implementation type.
     * @param alwaysProvide Intents this implementation always provide.
     * @param mayProvide  Intents this implementation may provide.
     */
    public ImplementationType(final QName name, Set<QName> alwaysProvide, Set<QName> mayProvide) {
        super(name);
        this.alwaysProvide = alwaysProvide;
        this.mayProvide = mayProvide;
    }

    /**
     * @return Intents this implementation always provide.
     */
    public Set<QName> getAlwaysProvide() {
        return Collections.unmodifiableSet(alwaysProvide);
    }

    /**
     * @return Intents this implementation may provide.
     */
    public Set<QName> getMayProvide() {
        return Collections.unmodifiableSet(mayProvide);
    }

}
