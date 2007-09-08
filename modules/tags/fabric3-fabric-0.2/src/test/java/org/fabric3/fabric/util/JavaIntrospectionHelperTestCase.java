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
package org.fabric3.fabric.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import junit.framework.TestCase;

public class JavaIntrospectionHelperTestCase extends TestCase {

    public JavaIntrospectionHelperTestCase() {
        super();
    }

    public JavaIntrospectionHelperTestCase(String arg0) {
        super(arg0);
    }

    public void testBean1AllPublicProtectedFields() throws Exception {
        Set<Field> beanFields = JavaIntrospectionHelper.getAllPublicAndProtectedFields(Bean1.class);
        assertEquals(4, beanFields.size());                //Bean1.ALL_BEAN1_PUBLIC_PROTECTED_FIELDS
    }

    public void testGetSuperAllMethods() throws Exception {
        Set<Method> superBeanMethods = JavaIntrospectionHelper.getAllUniquePublicProtectedMethods(SuperBean.class);
        assertEquals(SuperBean.ALL_SUPER_METHODS, superBeanMethods.size());
    }

    public void testGetBean1AllMethods() throws Exception {
        Set<Method> beanMethods = JavaIntrospectionHelper.getAllUniquePublicProtectedMethods(Bean1.class);
        assertEquals(Bean1.ALL_BEAN1_METHODS, beanMethods.size());
    }

    public void testOverrideMethod() throws Exception {
        Set<Method> beanFields = JavaIntrospectionHelper.getAllUniquePublicProtectedMethods(Bean1.class);
        boolean invoked = false;
        for (Method method : beanFields) {
            if (method.getName().equals("override")) {
                method.invoke(new Bean1(), "foo");
                invoked = true;
            }
        }
        if (!invoked) {
            throw new Exception("Override never invoked");
        }
    }

    public void testNoOverrideMethod() throws Exception {
        Set<Method> beanFields = JavaIntrospectionHelper.getAllUniquePublicProtectedMethods(Bean1.class);
        boolean found = false;
        for (Method method : beanFields) {
            if (method.getName().equals("noOverride") && method.getParameterTypes().length == 0) {
                found = true;
            }
        }
        if (!found) {
            throw new Exception("No override not found");
        }
    }

    public void testDefaultConstructor() throws Exception {
        Constructor ctr = JavaIntrospectionHelper.getDefaultConstructor(Bean2.class);
        assertEquals(ctr, Bean2.class.getConstructor());
        assertTrue(Bean2.class == ctr.newInstance((Object[]) null).getClass());
    }


    public void testGetAllInterfaces() {
        Set<Class> interfaces = JavaIntrospectionHelper.getAllInterfaces(Z.class);
        assertEquals(2, interfaces.size());
        assertTrue(interfaces.contains(W.class));
        assertTrue(interfaces.contains(W2.class));
    }


    public void testGetAllInterfacesObject() {
        Set<Class> interfaces = JavaIntrospectionHelper.getAllInterfaces(Object.class);
        assertEquals(0, interfaces.size());
    }

    public void testGetAllInterfacesNoInterfaces() {
        Set<Class> interfaces = JavaIntrospectionHelper.getAllInterfaces(NoInterface.class);
        assertEquals(0, interfaces.size());
    }


    public void setTestArray(Target[] array) {
    }

    private interface W {

    }

    private interface W2 {

    }

    private class X implements W {

    }

    private class Y extends X implements W, W2 {

    }

    private class Z extends Y {

    }

    private class NoInterface {

    }

    private interface Target {

        String getString();

        void setString(String val);

    }
}