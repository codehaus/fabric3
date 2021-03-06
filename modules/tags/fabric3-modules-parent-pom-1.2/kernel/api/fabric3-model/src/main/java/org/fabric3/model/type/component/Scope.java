/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.model.type.component;

import javax.xml.namespace.QName;

import org.osoa.sca.Conversation;

import org.fabric3.model.type.ModelObject;

/**
 * Defines the lifecycle for implementation instances.
 *
 * @version $Rev$ $Date$
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
