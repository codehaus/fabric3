/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.model.type.java;

import java.lang.reflect.Constructor;

/**
 * @version $Rev: 5481 $ $Date: 2008-09-26 02:36:30 -0700 (Fri, 26 Sep 2008) $
 */
public class ConstructorInjectionSite extends InjectionSite {
    private static final long serialVersionUID = -6543986170145816234L;
    private Signature signature;
    private int param;

    public ConstructorInjectionSite(Constructor<?> constructor, int param) {
        super(constructor.getParameterTypes()[param].getName());
        this.signature = new Signature(constructor);
        this.param = param;
    }

    public ConstructorInjectionSite(Signature signature, int param) {
        super(signature.getParameterTypes().get(param));
        this.signature = signature;
        this.param = param;
    }

    /**
     * Returns the signature that identifies the method.
     *
     * @return the signature that identifies the method
     */
    public Signature getSignature() {
        return signature;
    }

    /**
     * Returns the index of the parameter being injected.
     *
     * @return the index of the parameter being injected
     */
    public int getParam() {
        return param;
    }

    public String toString() {
        return signature.toString() + '[' + param + ']';
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConstructorInjectionSite that = (ConstructorInjectionSite) o;

        return param == that.param && signature.equals(that.signature);

    }

    public int hashCode() {
        return 31 * signature.hashCode() + param;
    }
}