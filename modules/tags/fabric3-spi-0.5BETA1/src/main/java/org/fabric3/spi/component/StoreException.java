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
package org.fabric3.spi.component;

import org.fabric3.host.Fabric3RuntimeException;

/**
 * @version $Rev$ $Date$
 */
public abstract class StoreException extends Fabric3RuntimeException {
    private static final long serialVersionUID = 3587233858463631351L;

    protected StoreException() {
    }

    protected StoreException(String message) {
        super(message);
    }

    protected StoreException(String message, String identifier) {
        super(message, identifier);
    }

    protected StoreException(String message, Throwable cause) {
        super(message, cause);
    }

    protected StoreException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }

    protected StoreException(Throwable cause) {
        super(cause);
    }
}
