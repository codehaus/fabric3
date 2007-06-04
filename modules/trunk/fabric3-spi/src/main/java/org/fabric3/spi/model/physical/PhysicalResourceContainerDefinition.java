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
package org.fabric3.spi.model.physical;

import java.net.URI;

/**
 * Base class for physical resource container definitions.  PhysicalResourceContainerDefinitions are marshalled to the
 * service nodes where they result in the creation of resource containers.
 *
 * @version $Rev$ $Date$
 */
public abstract class PhysicalResourceContainerDefinition {
    protected URI uri;

    protected PhysicalResourceContainerDefinition(URI name) {
        this.uri = name;
    }

    /**
     * Returns the classloader uri.
     *
     * @return the classloader uri
     */
    public URI getUri() {
        return uri;
    }

}
