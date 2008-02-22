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
package org.fabric3.introspection;

import java.lang.reflect.TypeVariable;
import java.util.Collection;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class TypeMappingTestCase extends TestCase {
    private TypeMapping typeMapping;
    private TypeVariable<Class<Types>> t;

    private static class Base {
    }

    private static class ExtendsBase extends Base {
    }

    private static class Types<T extends Base> {
        public T t;
        public T[] tArray;
        public Collection<T> tCollection;
    }

    public void testGetRawTypeForUnbound() throws NoSuchFieldException {
        assertEquals(String.class, typeMapping.getRawType(String.class));
        assertEquals(Base.class, typeMapping.getRawType(Types.class.getField("t").getGenericType()));
        assertEquals(Base[].class, typeMapping.getRawType(Types.class.getField("tArray").getGenericType()));
        assertEquals(Collection.class, typeMapping.getRawType(Types.class.getField("tCollection").getGenericType()));
    }

    public void testGetRawTypeForBound() throws NoSuchFieldException {
        typeMapping.addMapping(t, ExtendsBase.class);
        assertEquals(ExtendsBase.class, typeMapping.getRawType(Types.class.getField("t").getGenericType()));
        assertEquals(ExtendsBase[].class, typeMapping.getRawType(Types.class.getField("tArray").getGenericType()));
        assertEquals(Collection.class, typeMapping.getRawType(Types.class.getField("tCollection").getGenericType()));
    }

    protected void setUp() throws Exception {
        super.setUp();
        typeMapping = new TypeMapping();
        t = Types.class.getTypeParameters()[0];
    }
}
