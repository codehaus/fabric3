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

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;

/**
 * @version $Rev$ $Date$
 */
public class FieldInjectionSite extends InjectionSite {
    private static final long serialVersionUID = -6502983302874808563L;
    private String name;
    int modifiers;

    public FieldInjectionSite(Field field) {
        super(ElementType.FIELD, field.getType().getName());
        this.modifiers = field.getModifiers();
        name = field.getName();
    }

    /**
     * Gets the name of the field.
     *
     * @return Site name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the field modifiers.
     *
     * @return the field modifiers
     */
    public int getModifiers() {
        return modifiers;
    }

    public String toString() {
        return name;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldInjectionSite that = (FieldInjectionSite) o;
        return name.equals(that.name);

    }

    public int hashCode() {
        return name.hashCode();
    }
}
