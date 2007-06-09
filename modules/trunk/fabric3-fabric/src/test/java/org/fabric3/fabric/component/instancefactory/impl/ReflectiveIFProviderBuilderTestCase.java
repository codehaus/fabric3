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
package org.fabric3.fabric.component.instancefactory.impl;

import java.lang.annotation.ElementType;

import junit.framework.TestCase;
import org.fabric3.pojo.reflection.ReflectiveInstanceFactoryProvider;
import org.fabric3.fabric.model.physical.instancefactory.InjectionSiteMapping;
import org.fabric3.spi.model.instance.ValueSource;
import org.fabric3.spi.model.instance.ValueSource.ValueSourceType;
import org.fabric3.fabric.model.physical.instancefactory.MemberSite;
import org.fabric3.fabric.model.physical.instancefactory.ReflectiveIFProviderDefinition;
import org.fabric3.fabric.model.physical.instancefactory.Signature;

/**
 * @version $Date$ $Revision$
 */
public class ReflectiveIFProviderBuilderTestCase extends TestCase {
    private ReflectiveIFProviderBuilder builder = new ReflectiveIFProviderBuilder();
    private ReflectiveIFProviderDefinition definition;

    /**
     * Verifies an ValueSource is set properly for constructor parameters
     *
     * @throws Exception
     */
    public void testCdiSource() throws Exception {
        ValueSource cdiSource = new ValueSource(ValueSourceType.REFERENCE, "abc");
        definition.addCdiSource(cdiSource);

        ClassLoader cl = getClass().getClassLoader();
        ReflectiveInstanceFactoryProvider provider = builder.build(definition, cl);
        Class<?> clazz = provider.getMemberType(cdiSource);
        assertEquals(String.class, clazz);
    }

    /**
     * Verifies an ValueSource is set properly for protected fields
     *
     * @throws Exception
     */
    public void testProtectedFieldInjectionSource() throws Exception {
        ValueSource valueSource = new ValueSource(ValueSourceType.REFERENCE, "xyz");
        MemberSite memberSite = new MemberSite();
        memberSite.setName("xyz");
        memberSite.setElementType(ElementType.FIELD);
        InjectionSiteMapping mapping = new InjectionSiteMapping();
        mapping.setSite(memberSite);
        mapping.setSource(valueSource);
        definition.addInjectionSite(mapping);

        ClassLoader cl = getClass().getClassLoader();
        ReflectiveInstanceFactoryProvider provider = builder.build(definition, cl);
        Class<?> clazz = provider.getMemberType(valueSource);
        assertEquals(Bar.class, clazz);
    }

    /**
     * Verifies an ValueSource is set properly for setter methods
     *
     * @throws Exception
     */
    public void testMethodInjectionSource() throws Exception {
        ValueSource valueSource = new ValueSource(ValueSourceType.REFERENCE, "abc");
        MemberSite memberSite = new MemberSite();
        memberSite.setName("setAbc");
        memberSite.setElementType(ElementType.METHOD);
        InjectionSiteMapping mapping = new InjectionSiteMapping();
        mapping.setSite(memberSite);
        mapping.setSource(valueSource);
        definition.addInjectionSite(mapping);

        ClassLoader cl = getClass().getClassLoader();
        ReflectiveInstanceFactoryProvider provider = builder.build(definition, cl);
        Class<?> clazz = provider.getMemberType(valueSource);
        assertEquals(Bar.class, clazz);
    }


    protected void setUp() throws Exception {
        super.setUp();
        definition = new ReflectiveIFProviderDefinition();
        definition.setImplementationClass("org.fabric3.fabric.component.instancefactory.impl.Foo");
        definition.addConstructorArgument("java.lang.String");
        definition.addConstructorArgument("java.lang.Long");
        definition.setInitMethod(new Signature("init"));
        definition.setDestroyMethod(new Signature("destroy"));
    }
}
