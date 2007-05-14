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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The base class for assembly model types
 *
 * @version $Rev$ $Date$
 */
public abstract class ModelObject {
    private List<ResourceDescription> resources;

    protected ModelObject() {
    }

    /**
     * Returns the collection of resource descriptions for the type.
     *
     * @return the collection of resource descriptions for the type
     */
    public List<ResourceDescription> getResourceDescription() {
        if (resources == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(resources);
    }

    /**
     * Adds a resource description associated with the type. ResourceDescriptions identify runtime resources required by
     * the type.
     *
     * @param description the resource description to add
     */
    public void addResourceDescription(ResourceDescription description) {
        if (resources == null) {
            resources = new ArrayList<ResourceDescription>();
        }
        resources.add(description);
    }

}
