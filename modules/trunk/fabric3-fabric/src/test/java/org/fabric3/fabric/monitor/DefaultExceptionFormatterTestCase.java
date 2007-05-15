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
package org.fabric3.fabric.monitor;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;
import org.fabric3.host.Fabric3Exception;
import org.fabric3.host.Fabric3RuntimeException;

/**
 * @version $Rev$ $Date$
 */
public class DefaultExceptionFormatterTestCase extends TestCase {
    private DefaultExceptionFormatter formatter = new DefaultExceptionFormatter();

    public void testFabric3ExceptionFormat() throws Exception {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        Fabric3Exception e = new Fabric3Exception("somemessage") {
        };
        formatter.write(pw, e);
        assertTrue(writer.toString().indexOf("somemessage") >= 0);
    }

    public void testFabric3RuntimeExceptionFormat() throws Exception {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        Fabric3RuntimeException e = new Fabric3RuntimeException("somemessage") {
        };
        formatter.write(pw, e);
        assertTrue(writer.toString().indexOf("somemessage") >= 0);
    }

    public void testNormalExceptionFormat() throws Exception {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        Exception e = new Exception();
        formatter.write(pw, e); // just verify there are no errors since no formatting needs to be doen
    }

}
