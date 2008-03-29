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
package org.fabric3.fabric.assembly.resolver;

import java.net.URI;

import org.fabric3.spi.assembly.AssemblyException;

/**
 * Denotes an error resolving a wire target
 *
 * @version $Rev$ $Date$
 */
public abstract class ResolutionException extends AssemblyException {
    private static final long serialVersionUID = 2275834464327877714L;
    private URI source;
    private URI target;

    public ResolutionException(String message, URI source, URI target) {
        super(message);
        this.target = target;
        this.source = source;
    }

    public ResolutionException(String message, String identifier, URI source, URI target) {
        super(message, identifier);
        this.target = target;
        this.source = source;
    }

    public ResolutionException(String message, URI source, URI target, Throwable cause) {
        super(message, cause);
        this.target = target;
        this.source = source;
    }

    public ResolutionException(String message, String identifier, URI source, URI target, Throwable cause) {
        super(message, identifier, cause);
        this.target = target;
        this.source = source;
    }

    public ResolutionException(URI source, URI target) {
        this.target = target;
        this.source = source;
    }

    public ResolutionException(URI source, URI target, Throwable cause) {
        super(cause);
        this.target = target;
        this.source = source;
    }

    /**
     * Returns the wire source uri.
     *
     * @return wire source uri.
     */
    public URI getSource() {
        return source;
    }

    /**
     * Returns the wire target uri.
     *
     * @return wire target uri.
     */
    public URI getTarget() {
        return target;
    }


}
