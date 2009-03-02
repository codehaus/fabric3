/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.spi.builder;

import java.net.URI;


/**
 * Denotes a general error raised during wiring
 *
 * @version $Rev$ $Date$
 */
public class WiringException extends BuilderException {
    private static final long serialVersionUID = 3668451213570682938L;
    private URI sourceUri;
    private URI targetUri;

    public WiringException(Throwable cause) {
        super(cause);
    }

    public WiringException(String message, Throwable cause) {
        super(message, cause);
    }

    public WiringException(String message) {
        super(message);
    }

    public WiringException(String message, URI sourceUri, URI targetUri) {
        super(message);
        this.sourceUri = sourceUri;
        this.targetUri = targetUri;
    }

    public WiringException(String message, URI sourceUri, URI targetUri, Throwable cause) {
        super(message, cause);
        this.sourceUri = sourceUri;
        this.targetUri = targetUri;
    }

    public WiringException(String message, String identifier, URI sourceUri, URI targetUri) {
        super(message, identifier);
        this.sourceUri = sourceUri;
        this.targetUri = targetUri;
    }


    public WiringException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }

    public WiringException(String message, String identifier) {
        super(message, identifier);
    }

    /**
     * Returns the source name for the wire
     *
     * @return the source name the source name for the wire
     */
    public URI getSourceUri() {
        return sourceUri;
    }

    /**
     * Returns the target name for the wire
     *
     * @return the target name the source name for the wire
     */
    public URI getTargetUri() {
        return targetUri;
    }

}
