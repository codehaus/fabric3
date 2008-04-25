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
package org.fabric3.scdl.validation;

import org.fabric3.scdl.ComponentService;
import org.fabric3.scdl.ValidationFailure;

/**
 * Validation warning indicating that a component is trying to configure a service not defined by the component type.
 *
 * @version $Rev$ $Date$
 */
public class NoServiceInComponentType extends ValidationFailure<ComponentService> {
    public NoServiceInComponentType(ComponentService service) {
        super(service);
    }
}