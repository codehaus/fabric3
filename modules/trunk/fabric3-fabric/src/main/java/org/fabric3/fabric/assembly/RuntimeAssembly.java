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
package org.fabric3.fabric.assembly;

import java.net.URI;

import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.spi.assembly.Assembly;

/**
 * Represents an assembly that comprises a single runtime
 *
 * @version $Rev$ $Date$
 */
public interface RuntimeAssembly extends Assembly {

    /**
     * Instantitates a logical host component definition so that it may be wired to
     *
     * @param uri        the uri of the host component
     * @param definition the host component definition
     * @throws InstantiationException is an error occurs instantiating the logical component
     */
    void instantiateHostComponentDefinition(URI uri, ComponentDefinition<?> definition) throws InstantiationException;
}
