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
package org.fabric3.binding.jms.test.primitives;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class PrimitiveTest extends TestCase {

    @Reference
    protected PrimitiveService service;

    public void testInt() {
        assertEquals(1, service.testInt(1));
    }

    public void testIntArray() {
        int[] array = new int[]{1};
        assertEquals(1, service.testArrayInt(array)[0]);
    }

    public void testString() {
        assertEquals("test", service.testString("test"));
    }

}
