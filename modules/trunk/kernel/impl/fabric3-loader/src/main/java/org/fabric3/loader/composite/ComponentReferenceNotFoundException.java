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
package org.fabric3.loader.composite;

/**
 * @version $Rev$ $Date$
 */
public class ComponentReferenceNotFoundException extends CompositeLoaderException {
    private static final long serialVersionUID = -8504271004179432246L;
    private final String componentName;
    private final String name;

    public ComponentReferenceNotFoundException(String componentName, String name) {
        this.componentName = componentName;
        this.name = name;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return "The component type for component " + componentName + " does not have a reference " + name;
    }
}