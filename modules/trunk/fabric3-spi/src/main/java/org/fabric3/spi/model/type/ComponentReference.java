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
package org.fabric3.spi.model.type;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the configuration of a component reference.
 *
 * @version $Rev$ $Date$
 */
public class ComponentReference extends ModelObject {
    private final String name;
    private boolean autowire;
    private List<URI> targets = new ArrayList<URI>();

    /**
     * Construct a ComponentReference specifying the name of the reference being configured.
     *
     * @param name the name of the reference being configured
     */
    public ComponentReference(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns true if autowire is enabled for the reference.
     *
     * @return true if autowire is enabled for the reference.
     */
    public boolean isAutowire() {
        return autowire;
    }

    /**
     * Sets autowire enablement for the reference.
     *
     * @param autowire true if autowire is enabled.
     */
    public void setAutowire(boolean autowire) {
        this.autowire = autowire;
    }

    public List<URI> getTargets() {
        return targets;
    }

    public void addTarget(URI target) {
        targets.add(target);
    }

}
