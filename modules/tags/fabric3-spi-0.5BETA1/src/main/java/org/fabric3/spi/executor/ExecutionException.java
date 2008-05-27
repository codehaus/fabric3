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
package org.fabric3.spi.executor;

import org.fabric3.host.Fabric3Exception;

/**
 * Thrown when an error is encountered executing a Command on a runtime.
 *
 * @version $Rev$ $Date$
 */
public class ExecutionException extends Fabric3Exception {
    private static final long serialVersionUID = 7518538722801035000L;

    public ExecutionException(String message, String identifier) {
        super(message, identifier);
    }

    public ExecutionException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }

    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
