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
package org.fabric3.jpa.processor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import junit.framework.TestCase;

import org.fabric3.jpa.PersistenceUnitResource;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.pojo.scdl.JavaMappedResource;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.introspection.impl.DefaultContractProcessor;
import org.fabric3.introspection.InvalidServiceContractException;

/**
 * @version $Revision$ $Date$
 */
public class PersistenceUnitAnnotationProcessorTestCase extends TestCase {

    private PojoComponentType type;
    private PersistenceUnitAnnotationProcessor processor;

    public void setUp() throws InvalidServiceContractException {
        type = new PojoComponentType();
        processor = new PersistenceUnitAnnotationProcessor(new DefaultContractProcessor());
    }

    public void testValidField() throws Exception {

        Field field = Foo.class.getDeclaredField("emf1");
        processor.visitField(field, type, null);

        JavaMappedResource jmr = type.getResources().get("someName");
        assertNotNull(jmr);
        PersistenceUnitResource resource = PersistenceUnitResource.class.cast(jmr);
        assertEquals("someName", resource.getName());
        assertEquals("someUnitName", resource.getUnitName());

    }

    public void testInvalidFieldWithIncorrectType() throws Exception {

        try {
            Field field = Foo.class.getDeclaredField("emf2");
            processor.visitField(field, type, null);
        } catch (ProcessingException ex) {
            return;
        }

        fail("Expected processing exception");

    }

    public void testValidMethod() throws Exception {

        Method method = Foo.class.getDeclaredMethod("setEmf3", EntityManagerFactory.class);
        processor.visitMethod(method, type, null);

        JavaMappedResource jmr = type.getResources().get("someName");
        assertNotNull(jmr);
        PersistenceUnitResource resource = PersistenceUnitResource.class.cast(jmr);
        assertEquals("someName", resource.getName());
        assertEquals("someUnitName", resource.getUnitName());

    }

    public void testInvalidMethodWithIncorrectType() throws Exception {

        try {
            Method method = Foo.class.getDeclaredMethod("setEmf4", String.class);
            processor.visitMethod(method, type, null);
        } catch (ProcessingException ex) {
            return;
        }

        fail("Expected processing exception");

    }

    public void testInvalidMethodWithIncorrectNumberOfArguments() throws Exception {

        try {
            Method method = Foo.class.getDeclaredMethod("setEmf5", EntityManagerFactory.class, String.class);
            processor.visitMethod(method, type, null);
        } catch (ProcessingException ex) {
            return;
        }

        fail("Expected processing exception");

    }

    private static class Foo {

        @PersistenceUnit(name = "someName", unitName = "someUnitName")
        protected EntityManagerFactory emf1;

        @PersistenceUnit(name = "someName", unitName = "someUnitName")
        protected String emf2;

        @PersistenceUnit(name = "someName", unitName = "someUnitName")
        public void setEmf3(EntityManagerFactory emf) {
        }

        @PersistenceUnit(name = "someName", unitName = "someUnitName")
        public void setEmf4(String emf) {
        }

        @PersistenceUnit(name = "someName", unitName = "someUnitName")
        public void setEmf5(EntityManagerFactory emf, String name) {
        }

    }

}
