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
package org.fabric3.fabric.services.discovery;

import org.fabric3.spi.services.discovery.DiscoveryException;

/**
 * Denotes a timeout while performing a discovery operation.
 *
 * @version $Rev$ $Date$
 */
public class DiscoveryTimeoutException extends DiscoveryException {
    private static final long serialVersionUID = 7156621402105248064L;

    protected DiscoveryTimeoutException(String message) {
        super(message, null);
    }
}
