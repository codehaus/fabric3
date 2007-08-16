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
package org.fabric3.fabric.implementation.processor;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.pojo.processor.PojoComponentType;
import org.fabric3.pojo.processor.ProcessingException;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class EagerInitProcessorTestCase extends TestCase {

    public void testNoLevel() throws ProcessingException {
        EagerInitProcessor processor = new EagerInitProcessor();
        PojoComponentType type =
            new PojoComponentType(null);
        processor.visitClass(Level.class, type, null);
        assertEquals(50, type.getInitLevel());
    }

    public void testSubclass() throws ProcessingException {
        EagerInitProcessor processor = new EagerInitProcessor();
        PojoComponentType type =
            new PojoComponentType(null);
        processor.visitClass(SubClass.class, type, null);
        assertEquals(50, type.getInitLevel());
    }

    @EagerInit
    private class Level {
    }

    private class SubClass extends Level {

    }

}
