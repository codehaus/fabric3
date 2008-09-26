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
package org.fabric3.scdl;

import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class SignatureTestCase extends TestCase {

    public void testComplexType() throws Exception {
        Method method = Foo.class.getMethod("complex", Foo.class);
        Signature signature = new Signature("complex", Foo.class.getName());
        assertEquals(method, signature.getMethod(Foo.class));
    }

    public void testPrimitiveInt() throws Exception {
        Method method = Foo.class.getMethod("primitiveInt", Integer.TYPE);
        Signature signature = new Signature("primitiveInt", "int");
        assertEquals(method, signature.getMethod(Foo.class));
    }

    public void testPrimitiveDouble() throws Exception {
        Method method = Foo.class.getMethod("primitiveDouble", Double.TYPE);
        Signature signature = new Signature("primitiveDouble", "double");
        assertEquals(method, signature.getMethod(Foo.class));
    }

    public void testPrimitiveBoolean() throws Exception {
        Method method = Foo.class.getMethod("primitiveBoolean", Boolean.TYPE);
        Signature signature = new Signature("primitiveBoolean", "boolean");
        assertEquals(method, signature.getMethod(Foo.class));
    }

    public void testPrimitiveByte() throws Exception {
        Method method = Foo.class.getMethod("primitiveByte", Byte.TYPE);
        Signature signature = new Signature("primitiveByte", "byte");
        assertEquals(method, signature.getMethod(Foo.class));
    }

    public void testPrimitiveShort() throws Exception {
        Method method = Foo.class.getMethod("primitiveShort", Short.TYPE);
        Signature signature = new Signature("primitiveShort", "short");
        assertEquals(method, signature.getMethod(Foo.class));
    }

    public void testPrimitiveLong() throws Exception {
        Method method = Foo.class.getMethod("primitiveLong", Long.TYPE);
        Signature signature = new Signature("primitiveLong", "long");
        assertEquals(method, signature.getMethod(Foo.class));
    }

    public void testPrimitiveFloat() throws Exception {
        Method method = Foo.class.getMethod("primitiveFloat", Float.TYPE);
        Signature signature = new Signature("primitiveFloat", "float");
        assertEquals(method, signature.getMethod(Foo.class));
    }

    private class Foo {
        public void complex(Foo foo) {

        }

        public void primitiveInt(int i) {

        }

        public void primitiveDouble(double i) {

        }

        public void primitiveBoolean(boolean i) {

        }

        public void primitiveByte(byte i) {

        }

        public void primitiveShort(short i) {

        }

        public void primitiveLong(long i) {

        }

        public void primitiveFloat(float i) {

        }

    }
}
