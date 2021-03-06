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
package org.fabric3.fabric.instantiator;

import org.fabric3.host.Fabric3Exception;

/**
 * Denotes an exception instanitating a LogicalComponent
 *
 * @version $Rev$ $Date$
 */
public class LogicalInstantiationException extends Fabric3Exception {
    private static final long serialVersionUID = 4771407392907205266L;

    public LogicalInstantiationException(String message, String identifier) {
        super(message, identifier);
    }

    public LogicalInstantiationException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }

    public LogicalInstantiationException(Throwable cause) {
        super(cause);
    }

    public LogicalInstantiationException(String message) {
        super(message);
    }
}
