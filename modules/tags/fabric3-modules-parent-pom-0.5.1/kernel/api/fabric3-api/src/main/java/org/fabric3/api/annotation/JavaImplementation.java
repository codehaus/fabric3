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
package org.fabric3.api.annotation;

import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

import org.osoa.sca.Constants;
import org.osoa.sca.annotations.Intent;

/**
 * Annotation that indicates this class intends to use the SCA Java programming model.
 *
 * @version $Rev$ $Date$
 */
@Intent(targetNamespace = Constants.SCA_NS, localPart = "implementation.java")
@Retention(RUNTIME)
@Target(TYPE)
public @interface JavaImplementation {
}
