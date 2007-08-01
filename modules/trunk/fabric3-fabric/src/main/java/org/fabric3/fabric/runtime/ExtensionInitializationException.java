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
package org.fabric3.fabric.runtime;

import org.fabric3.host.runtime.InitializationException;

/**
 * @version $Rev$ $Date$
 */
public class ExtensionInitializationException extends InitializationException {
    public ExtensionInitializationException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }

    public ExtensionInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
