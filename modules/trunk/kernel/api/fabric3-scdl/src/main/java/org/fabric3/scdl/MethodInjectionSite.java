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
package org.fabric3.scdl;

import java.lang.reflect.Method;
import java.lang.annotation.ElementType;

/**
 * @version $Rev$ $Date$
 */
public class MethodInjectionSite extends InjectionSite {
    private Signature signature;
    private int param;

    public MethodInjectionSite(Method method, int param) {
        super(ElementType.METHOD, method.getParameterTypes()[param].getName());
        this.signature = new Signature(method);
        this.param = param;
    }

    /**
     * Returns the signature that identifies the method.
     * @return the signature that identifies the method
     */
    public Signature getSignature() {
        return signature;
    }

    /**
     * Returns the index of the parameter being injected.
     *
     * This will be 0 for a normal setter method.
     *
     * @return the index of the parameter being injected
     */
    public int getParam() {
        return param;
    }

    public String toString() {
        return signature.toString() + '[' + param + ']';
    }
}
