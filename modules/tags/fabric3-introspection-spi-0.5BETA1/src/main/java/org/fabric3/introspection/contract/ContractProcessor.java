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
package org.fabric3.introspection.contract;

import java.lang.reflect.Type;

import org.fabric3.introspection.TypeMapping;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ValidationContext;

/**
 * Interface for processors that can construct a ServiceContract from a Java type.
 *
 * @version $Rev$ $Date$
 */
public interface ContractProcessor {
    /**
     * Introspect a Java Type (e.g. a Class) and return the ServiceContract. If validation errors or warnings are encountered, they will be reported
     * in the ValidationContext.
     *
     * @param typeMapping the type mapping for the interface
     * @param type        the Java Type to introspect
     * @param context     the validation context for reporting errors and warnings
     * @return the ServiceContract corresponding to the interface type
     */
    ServiceContract<Type> introspect(TypeMapping typeMapping, Type type, ValidationContext context);
}
