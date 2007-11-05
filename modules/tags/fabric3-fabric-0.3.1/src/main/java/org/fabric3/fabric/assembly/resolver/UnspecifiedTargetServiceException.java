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
package org.fabric3.fabric.assembly.resolver;

import java.net.URI;

import org.fabric3.fabric.assembly.ResolutionException;

/**
 * Thrown when a target service on a component has not been specified and it is required, e.g. when the target component
 * implements more than one service.
 *
 * @version $Rev$ $Date$
 */
public class UnspecifiedTargetServiceException extends ResolutionException {
    private static final long serialVersionUID = -8334126598054159339L;
    private URI source;

    public UnspecifiedTargetServiceException(String message, URI source) {
        super(message, source, null);
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

}
