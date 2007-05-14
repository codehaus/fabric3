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
package org.fabric3.spi.model.instance;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fabric3.spi.model.type.ReferenceDefinition;

/**
 * Represents a resolved reference
 *
 * @version $Rev$ $Date$
 */
public class LogicalReference extends Referenceable {
    private final List<LogicalBinding> bindings;
    private final ReferenceDefinition definition;
    private List<URI> targets;

    public LogicalReference(URI uri, ReferenceDefinition definition) {
        super(uri);
        this.definition = definition;
        bindings = new ArrayList<LogicalBinding>();
        targets = new ArrayList<URI>();
    }

    public ReferenceDefinition getDefinition() {
        return definition;
    }

    public List<LogicalBinding> getBindings() {
        return Collections.unmodifiableList(bindings);
    }

    public void addBinding(LogicalBinding binding) {
        bindings.add(binding);
    }

    public List<URI> getTargetUris() {
        return Collections.unmodifiableList(targets);
    }

    public void addTargetUri(URI uri) {
        targets.add(uri);
    }

}
