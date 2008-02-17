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
package org.fabric3.fabric.services.instancefactory;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.scdl.InjectionSiteMapping;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.Signature;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuildHelper;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.scdl.ValueSource;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.ValueSource.ValueSourceType;
import org.fabric3.fabric.services.instancefactory.ReflectiveInstanceFactoryBuilder;
import org.fabric3.fabric.services.classloading.ClassLoaderRegistryImpl;

/**
 * @version $Date$ $Revision$
 */
public class ReflectiveIFProviderBuilderTestCase extends TestCase {
    private InstanceFactoryBuildHelper helper = new BuildHelperImpl(new ClassLoaderRegistryImpl());
    private ReflectiveInstanceFactoryBuilder builder = new ReflectiveInstanceFactoryBuilder(null, helper);
    private InstanceFactoryDefinition definition;
    private Constructor<Foo> constructor;
    private ClassLoader cl;

    /**
     * Verifies an ValueSource is set properly for constructor parameters
     *
     * @throws Exception
     */
    public void testCdiSource() throws Exception {
        InstanceFactoryProvider provider = builder.build(definition, cl);
        assertEquals(String.class, provider.getMemberType(new ValueSource(ValueSourceType.PROPERTY, "a")));
    }

    /**
     * Verifies an ValueSource is set properly for protected fields
     *
     * @throws Exception
     */
    public void testProtectedFieldInjectionSource() throws Exception {
        ValueSource valueSource = new ValueSource(ValueSourceType.REFERENCE, "xyz");
        Field field = Foo.class.getDeclaredField("xyz");
        InjectionSite injectionSite = new FieldInjectionSite(field);
        InjectionSiteMapping mapping = new InjectionSiteMapping();
        mapping.setSite(injectionSite);
        mapping.setSource(valueSource);
        definition.addInjectionSite(mapping);

        InstanceFactoryProvider provider = builder.build(definition, cl);
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
        Method method = Foo.class.getMethod("setAbc", Bar.class);
        InjectionSite injectionSite = new MethodInjectionSite(method, 0);
        InjectionSiteMapping mapping = new InjectionSiteMapping();
        mapping.setSite(injectionSite);
        mapping.setSource(valueSource);
        definition.addInjectionSite(mapping);

        InstanceFactoryProvider provider = builder.build(definition, cl);
        Class<?> clazz = provider.getMemberType(valueSource);
        assertEquals(Bar.class, clazz);
    }


    protected void setUp() throws Exception {
        super.setUp();
        cl = getClass().getClassLoader();
        constructor = Foo.class.getConstructor(String.class, Long.class);

        definition = new InstanceFactoryDefinition();
        definition.setImplementationClass(Foo.class.getName());
        definition.setConstructor(new Signature(constructor));
        definition.setInitMethod(new Signature("init"));
        definition.setDestroyMethod(new Signature("destroy"));
        definition.addInjectionSite(createMapping("a", 0, ValueSourceType.PROPERTY));
        definition.addInjectionSite(createMapping("b", 1, ValueSourceType.REFERENCE));
    }

    private InjectionSiteMapping createMapping(String name, int pos, ValueSourceType type) {
        InjectionSite site = new ConstructorInjectionSite(constructor, pos);
        ValueSource source = new ValueSource(type, name);
        InjectionSiteMapping mapping = new InjectionSiteMapping();
        mapping.setSource(source);
        mapping.setSite(site);
        return mapping;
    }

    public static class Foo {

        protected Bar xyz;

        public Foo(String a, Long b) {
        }

        public void setAbc(Bar abc) {
        }

        public void init() {
        }

        public void destroy() {
        }

    }
    
    public static class Bar {

    }
}
