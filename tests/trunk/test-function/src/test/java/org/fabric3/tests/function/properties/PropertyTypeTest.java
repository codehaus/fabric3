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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
public class PropertyTypeTest extends TestCase {
    @Reference
    public PropertyTypes service;

    public void testBoolean() {
        assertEquals(true, service.getBooleanPrimitive());
    }

    public void testByte() {
        assertEquals((byte)12, service.getBytePrimitive());
    }

    public void testShort() {
        assertEquals((short)1234, service.getShortPrimitive());
    }

    public void testInteger() {
        assertEquals(12345678, service.getIntPrimitive());
    }

    public void testLong() {
        assertEquals(123451234512345l, service.getLongPrimitive());
    }

    public void testFloat() {
        assertEquals(1.2345f, service.getFloatPrimitive());
    }

    public void testDouble() {
        assertEquals(1.2345e10, service.getDoublePrimitive());
    }

    public void testString() {
        assertEquals("Hello World", service.getString());
    }

    public void testBooleanValue() {
        assertEquals(Boolean.TRUE, service.getBooleanValue());
    }

    public void testByteValue() {
        assertEquals(Byte.valueOf((byte)12), service.getByteValue());
    }

    public void testShortValue() {
        assertEquals(Short.valueOf((short)1234), service.getShortValue());
    }

    public void testIntegerValue() {
        assertEquals(Integer.valueOf(12345678), service.getIntegerValue());
    }

    public void testLongValue() {
        assertEquals(Long.valueOf(123451234512345l), service.getLongValue());
    }

    public void testFloatValue() {
        assertEquals(1.2345f, service.getFloatValue());
    }

    public void testDoubleValue() {
        assertEquals(1.2345e10, service.getDoubleValue());
    }

    public void testClassValue() {
        assertEquals(PropertyTypes.class, service.getClassValue());
    }

    public void testURI() {
        assertEquals(URI.create("urn:fabric3:test"), service.getUriValue());
    }

    public void testURL() throws MalformedURLException {
        assertEquals(new URL("file://./root"), service.getUrlValue());
    }

    public void testDate() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.clear();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2007, Calendar.OCTOBER, 31, 0, 0, 0);
       // assertEquals(calendar.getTime(), service.getDateValue());
    }

    public void testCalendar() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.clear();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2007, Calendar.OCTOBER, 31, 1, 0, 0);
        
        assertEquals(calendar.getTime(), service.getCalendarValue().getTime());
    }
}
