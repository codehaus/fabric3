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
package org.fabric3.fabric.services.formatter;

import java.io.PrintWriter;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import org.fabric3.fabric.services.formatter.DefaultExceptionFormatter;

/**
 * @version $Rev$ $Date$
 */
public class DefaultExceptionFormatterTestCase extends TestCase {
    private DefaultExceptionFormatter formatter;
    private PrintWriter writer;
    private Exception cause;

    public void testType() {
        assertEquals(Throwable.class, formatter.getType());
    }

    public void testFormat() {
        cause.printStackTrace(writer);
        EasyMock.replay(writer);
        EasyMock.replay(cause);
        formatter.write(writer, cause);
        EasyMock.verify(writer);
        EasyMock.verify(cause);
    }

    protected void setUp() throws Exception {
        super.setUp();
        formatter = new DefaultExceptionFormatter();
        writer = EasyMock.createMock(PrintWriter.class);
        cause = EasyMock.createMock(Exception.class, new Method[]{Exception.class.getMethod("printStackTrace", PrintWriter.class)});
    }
}
