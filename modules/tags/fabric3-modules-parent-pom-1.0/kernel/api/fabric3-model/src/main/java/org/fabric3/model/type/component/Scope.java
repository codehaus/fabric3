/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
package org.fabric3.model.type.component;

import javax.xml.namespace.QName;

import org.osoa.sca.Conversation;

import org.fabric3.model.type.ModelObject;

/**
 * An implementation scope that defines the lifecycle for implementation instances.
 *
 * @version $Rev: 5822 $ $Date: 2008-11-05 11:31:03 -0800 (Wed, 05 Nov 2008) $
 * @param <T> the type of identifier used to identify instances of this scope
 */
public class Scope<T> extends ModelObject {
    private static final long serialVersionUID = -5300929173662672089L;
    public static final Scope<Object> STATELESS = new Scope<Object>("STATELESS", Object.class);
    public static final Scope<Conversation> CONVERSATION = new Scope<Conversation>("CONVERSATION", Conversation.class);
    public static final Scope<QName> COMPOSITE = new Scope<QName>("COMPOSITE", QName.class);

    private final Class<T> identifierType;
    private final String scope;

    public Scope(String scope, Class<T> identifierType) {
        assert scope != null && identifierType != null;
        this.scope = scope.toUpperCase().intern();
        this.identifierType = identifierType;
    }

    public String getScope() {
        return scope;
    }

    public Class<T> getIdentifierType() {
        return identifierType;
    }

    @SuppressWarnings({"StringEquality"})
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Scope scope1 = (Scope) o;
        return scope == scope1.scope;
    }

    public int hashCode() {
        return scope.hashCode();
    }

    public String toString() {
        return scope;
    }
}
