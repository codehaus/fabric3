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
package org.fabric3.introspection.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.Constructor;

import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.IntrospectionHelper;

/**
 * @version $Rev$ $Date$
 */
public class DefaultIntrospectionHelper implements IntrospectionHelper {
    public String getSiteName(Field field, String override) throws IntrospectionException {
        if (override != null && override.length() != 0) {
            return override;
        }
        return field.getName();
    }

    public String getSiteName(Method setter, String override) throws IntrospectionException {
        if (override != null && override.length() != 0) {
            return override;
        }
        String name = setter.getName();
        if (name.length() > 3 && name.startsWith("set")) {
            name = Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        return name;
    }

    public Type getType(Method setter) throws IntrospectionException {
        return getType(setter, 0);
    }

    public Type getType(Method method, int index) throws IntrospectionException {
        return method.getGenericParameterTypes()[index];
    }

    public Type getType(Constructor constructor, int index) throws IntrospectionException {
        return constructor.getGenericParameterTypes()[index];
    }
}
