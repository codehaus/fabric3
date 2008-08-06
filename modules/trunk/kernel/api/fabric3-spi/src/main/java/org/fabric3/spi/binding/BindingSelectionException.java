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
package org.fabric3.spi.binding;

import org.fabric3.host.Fabric3Exception;

/**
 * General error denoting an exception configuring a binding.
 *
 * @version $Revision$ $Date$
 */
public class BindingSelectionException extends Fabric3Exception {
    private static final long serialVersionUID = -8232393072665476184L;

    public BindingSelectionException(String message) {
        super(message);
    }

    public BindingSelectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
