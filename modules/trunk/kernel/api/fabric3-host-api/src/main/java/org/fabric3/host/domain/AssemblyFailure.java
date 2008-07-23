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
package org.fabric3.host.domain;

import java.net.URI;

/**
 * Base class for recoverable errors updating the domain assembly encountered during a deployment.
 *
 * @version $Revision$ $Date$
 */
public abstract class AssemblyFailure {
    private URI componentUri;

    /**
     * Constructor.
     *
     * @param componentUri the URI of the component associated with the failure.
     */
    public AssemblyFailure(URI componentUri) {
        this.componentUri = componentUri;
    }

    /**
     * Returns the URI of the component associated with the failure.
     *
     * @return the URI of the component associated with the failure.
     */
    public URI getComponentUri() {
        return componentUri;
    }

    /**
     * Returns the error message.
     *
     * @return the error message.
     */
    public String getMessage() {
        return "";
    }
}
