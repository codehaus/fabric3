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

import junit.framework.TestCase;

import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.PojoComponentType;

/**
 * @version $Rev$ $Date$
 */
public class ScopeProcessorTestCase extends TestCase {

    private ScopeProcessor processor;
    private PojoComponentType type;

    public void testScopeFromAnnotation() throws ProcessingException {
        processor.visitClass(Composite.class, type, null);
        assertEquals("COMPOSITE", type.getScope());
    }

    public void testScopeFromBareClass() throws ProcessingException {
        processor.visitClass(None.class, type, null);
        assertNull(type.getScope());
    }

    protected void setUp() throws Exception {
        super.setUp();
        type = new PojoComponentType(null);
        processor = new ScopeProcessor();
    }

    @org.osoa.sca.annotations.Scope("COMPOSITE")
    private class Composite {
    }

    private class None {
    }

}
