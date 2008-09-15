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

import org.fabric3.host.domain.AssemblyFailure;
import org.fabric3.spi.model.instance.LogicalComponent;

public class ReferenceNotFound extends AssemblyFailure {
    private String message;
    private LogicalComponent<?> component;
    private String referenceName;

    public ReferenceNotFound(String message, LogicalComponent<?> component, String referenceName) {
        super(component.getUri());
        this.message = message;
        this.component = component;
        this.referenceName = referenceName;
    }

    public LogicalComponent<?> getComponent() {
        return component;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getMessage() {
        return message;
    }
}
