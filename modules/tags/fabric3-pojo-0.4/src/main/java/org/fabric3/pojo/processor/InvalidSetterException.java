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
package org.fabric3.pojo.processor;

import java.lang.reflect.Method;

/**
 * Exception indicating that a method annotated as an injection site does not meet the requirements.
 *
 * Such methods must be:
 * <ul>
 * <li>protected or private</li>
 * <li>instance methods (not static or synthetic)</li>
 * <li>have a void return type</li>
 * <li>have a single parameter</li>
 * </ul>
 *
 * @version $Rev$ $Date$
 */
public class InvalidSetterException extends ProcessingException {

    /**
     * Constructor taking a default message and an identifier for the method.
     *
     * @param method the invalid setter method
     */
    public InvalidSetterException(Method method) {
        super(null, method.toString());
        setMember(method);
    }

    public String getMessage() {
        return getIdentifier();
    }
}
