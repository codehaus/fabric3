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
package org.fabric3.scdl;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Base class for SCA definitions that support references to intents and policySets.
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractPolicyAware extends ModelObject implements PolicyAware {

    private Set<QName> intents = new LinkedHashSet<QName>();
    private Set<QName> policySets = new LinkedHashSet<QName>();

    public Set<QName> getIntents() {
        return Collections.unmodifiableSet(intents);
    }

    public Set<QName> getPolicySets() {
        return Collections.unmodifiableSet(policySets);
    }

    public void setIntents(Set<QName> intents) {
        this.intents = intents;
    }

    public void addIntent(QName intent) {
        intents.add(intent);
    }

    public void setPolicySets(Set<QName> policySets) {
        this.policySets = policySets;
    }

}
