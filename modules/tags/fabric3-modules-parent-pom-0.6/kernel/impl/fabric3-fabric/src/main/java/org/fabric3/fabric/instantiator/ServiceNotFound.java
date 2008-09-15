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
package org.fabric3.fabric.instantiator;

import java.net.URI;

import org.fabric3.host.domain.AssemblyFailure;
import org.fabric3.spi.model.instance.Bindable;

public class ServiceNotFound extends AssemblyFailure {
    private String message;
    private Bindable bindable;
    private URI targetUri;

    public ServiceNotFound(String message, Bindable bindable, URI targetUri) {
        super(bindable.getParent().getUri());
        this.message = message;
        this.bindable = bindable;
        this.targetUri = targetUri;
    }

    public Bindable getBindable() {
        return bindable;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public String getMessage() {
        return message;
    }
}
