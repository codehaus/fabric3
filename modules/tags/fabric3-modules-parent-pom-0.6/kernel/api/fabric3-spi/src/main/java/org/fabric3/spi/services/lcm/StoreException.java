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
package org.fabric3.spi.services.lcm;

import org.fabric3.host.Fabric3Exception;


/**
 * Denotes an exception recording an logical component operation
 *
 * @version $Rev$ $Date$
 */
public class StoreException extends Fabric3Exception {
    private static final long serialVersionUID = 652954682135057498L;

    public StoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public StoreException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }
}
