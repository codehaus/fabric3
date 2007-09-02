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
package org.fabric3.tests.function.properties;

import org.osoa.sca.annotations.Reference;
import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class PropertyTypeTest extends TestCase {
    @Reference
    public PropertyTypes service;

    public void testBoolean() {
        assertEquals(true, service.getBoolean());
    }

    public void testByte() {
        assertEquals((byte)12, service.getByte());
    }

    public void testShort() {
        assertEquals((short)1234, service.getShort());
    }

    public void testInteger() {
        assertEquals(12345678, service.getInt());
    }

    public void testLong() {
        assertEquals(123451234512345l, service.getLong());
    }

    public void testFloat() {
        assertEquals(1.2345f, service.getFloat());
    }

    public void testDouble() {
        assertEquals(1.2345e10, service.getDouble());
    }

    public void testString() {
        assertEquals("Hello World", service.getPropertyValue(String.class));
    }

    public void testBooleanValue() {
        assertEquals(Boolean.TRUE, service.getPropertyValue(Boolean.class));
    }

    public void testByteValue() {
        assertEquals(Byte.valueOf((byte)12), service.getPropertyValue(Byte.class));
    }

    public void testShortValue() {
        assertEquals(Short.valueOf((short)1234), service.getPropertyValue(Short.class));
    }

    public void testIntegerValue() {
        assertEquals(Integer.valueOf(12345678), service.getPropertyValue(Integer.class));
    }

    public void testLongValue() {
        assertEquals(Long.valueOf(123451234512345l), service.getPropertyValue(Long.class));
    }

    public void testFloatValue() {
        assertEquals(1.2345f, service.getPropertyValue(Float.class));
    }

    public void testDoubleValue() {
        assertEquals(1.2345e10, service.getPropertyValue(Double.class));
    }

    public void testClassValue() {
        assertEquals(PropertyTypes.class, service.getPropertyValue(Class.class));
    }
}
