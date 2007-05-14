/*
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
package org.fabric3.api;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class Fabric3RuntimeExceptionTestCase extends TestCase {
    private static final Throwable CAUSE = new Throwable("Cause");
    private static final String MESSAGE = "Message";
    private static final String IDENTIFIER = "IDENTIFIER";
    private static final String CONTEXT1 = "CONTEXT1";
    private static final String CONTEXT2 = "CONTEXT2";

    public void testNoArgConstructor() {
        Fabric3RuntimeException ex = new DummyException();
        assertNull(ex.getMessage());
        assertNull(ex.getCause());
        assertNull(ex.getIdentifier());
    }

    public void testMessageConstructor() {
        Fabric3RuntimeException ex = new DummyException(MESSAGE);
        assertSame(MESSAGE, ex.getMessage());
        assertNull(ex.getCause());
        assertNull(ex.getIdentifier());
    }

    public void testThrowableConstructor() {
        Fabric3RuntimeException ex = new DummyException(CAUSE);
        assertEquals(CAUSE.getClass().getName() + ": " + CAUSE.getMessage(), ex.getMessage());
        assertSame(CAUSE, ex.getCause());
        assertNull(ex.getIdentifier());
    }

    public void testMessageThrowableConstructor() {
        Fabric3RuntimeException ex = new DummyException(MESSAGE, CAUSE);
        assertSame(MESSAGE, ex.getMessage());
        assertSame(CAUSE, ex.getCause());
        assertNull(ex.getIdentifier());
    }

    public void testIdentifier() {
        Fabric3RuntimeException ex = new DummyException(MESSAGE, IDENTIFIER);
        assertEquals(IDENTIFIER, ex.getIdentifier());
    }

    public void testAppendBaseMessage() {
        Fabric3RuntimeException ex = new DummyException(MESSAGE, IDENTIFIER);
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        ex.appendBaseMessage(pw);
        assertEquals("Message [IDENTIFIER]", writer.toString());
    }

    public void testAppendBaseMessageNoIdentifier() {
        Fabric3RuntimeException ex = new DummyException(MESSAGE);
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        ex.appendBaseMessage(pw);
        assertEquals("Message", writer.toString());
    }

    public static class DummyException extends Fabric3RuntimeException {
        public DummyException() {
        }

        public DummyException(String message) {
            super(message);
        }

        public DummyException(String message, String identifier) {
            super(message, identifier);
        }

        public DummyException(String message, Throwable cause) {
            super(message, cause);
        }

        public DummyException(Throwable cause) {
            super(cause);
        }
    }
}
