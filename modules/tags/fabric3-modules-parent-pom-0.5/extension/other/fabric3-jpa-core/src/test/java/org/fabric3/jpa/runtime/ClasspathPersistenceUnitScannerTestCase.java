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
package org.fabric3.jpa.runtime;

import javax.persistence.spi.PersistenceUnitInfo;

import org.fabric3.jpa.runtime.Fabric3JpaRuntimeException;
import org.fabric3.jpa.runtime.ClasspathPersistenceUnitScanner;
import org.fabric3.jpa.runtime.PersistenceUnitScanner;

import junit.framework.TestCase;

/**
 *
 * @version $Revision$ $Date$
 */
public class ClasspathPersistenceUnitScannerTestCase extends TestCase {
    
    private PersistenceUnitScanner scanner;

    protected void setUp() throws Exception {
        scanner = new ClasspathPersistenceUnitScanner();
    }

    public void testGetPersistenceUnitInfo() {
        PersistenceUnitInfo info = scanner.getPersistenceUnitInfo("test", getClass().getClassLoader());
        assertNotNull(info);
    }

    public void testGetNonExistentPersistenceUnitInfo() {
        try {
            scanner.getPersistenceUnitInfo("test1", getClass().getClassLoader());
            fail("Expected Exception");
        } catch(Fabric3JpaRuntimeException ex) {
        }
    }

}
