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
package org.fabric3.fabric.instantiator.target;

import java.net.URI;

import org.fabric3.host.domain.AssemblyFailure;
import org.fabric3.spi.model.instance.LogicalReference;

public class TargetComponentNotFound extends AssemblyFailure {
    private LogicalReference reference;
    private URI targetUri;

    public TargetComponentNotFound(LogicalReference reference, URI targetUri) {
        super(reference.getUri());
        this.reference = reference;
        this.targetUri = targetUri;
    }

    public LogicalReference getReference() {
        return reference;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public String getMessage() {
        return "Target component component for reference " + reference.getUri() + " not found: " + targetUri;
    }

}
