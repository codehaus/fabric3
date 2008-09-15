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
package org.fabric3.pojo.reflection;

import org.fabric3.spi.ObjectCreationException;

/**
 * @version $Rev$ $Date$
 */
public class NullPrimitiveException extends ObjectCreationException {
    private static final long serialVersionUID = 4043316381690250609L;
    private final int param;

    public NullPrimitiveException(String identifier, int param) {
        super(null, identifier);
        this.param = param;
    }

    public String getMessage() {
        return "Cannot assign null value to primitive for parameter " + param + " of " + getIdentifier();
    }
}