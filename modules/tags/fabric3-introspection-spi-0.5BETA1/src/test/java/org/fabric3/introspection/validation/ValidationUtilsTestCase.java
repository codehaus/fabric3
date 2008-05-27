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
package org.fabric3.introspection.validation;

import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.fabric3.host.contribution.ValidationFailure;

/**
 * @version $Revision$ $Date$
 */
public class ValidationUtilsTestCase extends TestCase {
    private List<ValidationFailure> failures;

    public void testWriteErrors() throws Exception {
        String output = ValidationUtils.outputErrors(failures);
        assertTrue(output.indexOf("this is a test") > 0);
    }

    public void testWriteWarnings() throws Exception {
        String output = ValidationUtils.outputWarnings(failures);
        assertTrue(output.indexOf("this is a test") > 0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        failures = new ArrayList<ValidationFailure>();
        failures.add(new Failure());
    }

    private static class Failure extends ValidationFailure<Object> {

        public Failure() {
            super(null);
        }

        public String getMessage() {
            return "this is a test";
        }
    }
}
