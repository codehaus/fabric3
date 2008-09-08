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
package org.fabric3.jetty;

import org.fabric3.host.Fabric3Exception;

/**
 * @version $Rev$ $Date$
 */
public class JettyInitializationException extends Fabric3Exception {
    private static final long serialVersionUID = 1918582897250667465L;

    public JettyInitializationException(String message) {
        super(message);
    }

    public JettyInitializationException(String message, String identifier) {
        super(message, identifier);
    }

    public JettyInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public JettyInitializationException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }
}
