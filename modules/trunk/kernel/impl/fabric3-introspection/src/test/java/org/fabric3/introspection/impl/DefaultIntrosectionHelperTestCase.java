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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Collections;

import junit.framework.TestCase;

import org.fabric3.introspection.TypeMapping;

/**
 * @version $Rev$ $Date$
 */
public class DefaultIntrosectionHelperTestCase extends TestCase {
    private DefaultIntrospectionHelper helper;
    private TypeMapping boundMapping;

    private static interface SuperInterface {
    }

    private static interface ServiceInterface extends SuperInterface {
    }

    private static interface SubInterface extends ServiceInterface {
    }

    private static class Base {
    }

    private static class BaseWithInterface implements ServiceInterface {
    }

    private static class ExtendsBaseWithInterface extends BaseWithInterface {
    }

    private static class ExtendsBaseWithSubInterface extends BaseWithInterface implements SubInterface {
    }

    private static class ExtendsBase extends Base {
    }

    private static class BaseTypes<T extends Base> {
        public T t;
        public Collection<String> stringCollection;
        public Map<String, Integer> intMap;

        public T[] tArray;
        public Collection<T> tCollection;
        public Map<String, T> tMap;
    }

    private static class BoundTypes extends BaseTypes<ExtendsBase> {
    }

    public void testTypeMappingBoundTypes() {
        assertEquals(ExtendsBase.class, boundMapping.getActualType(getType(BaseTypes.class, "t")));
    }

    public void testBaseType() {
        assertEquals(String.class, helper.getBaseType(String.class));
        assertEquals(int.class, helper.getBaseType(int.class));
        assertEquals(int.class, helper.getBaseType(Integer.TYPE));
        assertEquals(Integer.class, helper.getBaseType(Integer.class));

        assertEquals(int.class, helper.getBaseType(int[].class));
        assertEquals(String.class, helper.getBaseType(String[].class));

        assertEquals(String.class, helper.getBaseType(getType(BaseTypes.class, "stringCollection")));
        assertEquals(Integer.class, helper.getBaseType(getType(BaseTypes.class, "intMap")));

        assertEquals(Base.class, helper.getBaseType(getType(BaseTypes.class, "tArray")));
        assertEquals(Base.class, helper.getBaseType(getType(BaseTypes.class, "tCollection")));
        assertEquals(Base.class, helper.getBaseType(getType(BaseTypes.class, "tMap")));
    }

    public void testBoundTypes() {
        // FABRICTHREE-216 the helper should return the actual bound type but to do this is needs the actual type mapping from BoundTypes
        assertEquals(Base.class, helper.getBaseType(getType(BoundTypes.class, "tArray")));
        assertEquals(Base.class, helper.getBaseType(getType(BoundTypes.class, "tCollection")));
//        assertEquals(ExtendsBase.class, helper.getBaseType(getType(BoundTypes.class, "tArray")));
//        assertEquals(ExtendsBase.class, helper.getBaseType(getType(BoundTypes.class, "tCollection")));
    }

    protected Type getType(Class<?> type, String fieldName) {
        try {
            return type.getField(fieldName).getGenericType();
        } catch (NoSuchFieldException e) {
            throw new AssertionError(fieldName);
        }
    }

    public void testImplementedInterfacesWithNoInterfaces() {
        assertTrue(helper.getImplementedInterfaces(Base.class).isEmpty());
        assertTrue(helper.getImplementedInterfaces(ExtendsBase.class).isEmpty());
    }

    public void testImplementedInterfaces() {
        assertEquals(Collections.singleton(ServiceInterface.class), helper.getImplementedInterfaces(BaseWithInterface.class));
        assertEquals(Collections.singleton(ServiceInterface.class), helper.getImplementedInterfaces(ExtendsBaseWithInterface.class));
        assertEquals(Collections.singleton(SubInterface.class), helper.getImplementedInterfaces(ExtendsBaseWithSubInterface.class));
    }

    protected void setUp() throws Exception {
        super.setUp();
        helper = new DefaultIntrospectionHelper();
        boundMapping = helper.mapTypeParameters(BoundTypes.class);
    }
}
