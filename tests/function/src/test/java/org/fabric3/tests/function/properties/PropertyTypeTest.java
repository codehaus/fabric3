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

    public void testString() {
        assertEquals("Hello World", service.getPropertyValue(String.class));
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
}
