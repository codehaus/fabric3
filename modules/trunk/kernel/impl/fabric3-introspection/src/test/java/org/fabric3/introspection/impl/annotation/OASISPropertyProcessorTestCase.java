/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.introspection.impl.annotation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.oasisopen.sca.annotation.Property;

import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.model.type.component.AbstractComponentType;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;

@SuppressWarnings("unchecked")
public class OASISPropertyProcessorTestCase extends TestCase {
    private OASISPropertyProcessor<Implementation<? extends InjectingComponentType>> processor;


    public void testInvalidMethodAccessor() throws Exception {
        Method method = TestPrivateClass.class.getDeclaredMethod("setRequiredProperty", String.class);
        Property annotation = method.getAnnotation(Property.class);
        TypeMapping mapping = new TypeMapping();
        IntrospectionContext context = new DefaultIntrospectionContext(null, null, null, null, mapping);

        processor.visitMethod(annotation, method, new TestImplementation(), context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof InvalidAccessor);
    }

    public void testInvalidFieldAccessor() throws Exception {
        Field field = TestPrivateClass.class.getDeclaredField("requiredProperty");
        Property annotation = field.getAnnotation(Property.class);
        TypeMapping mapping = new TypeMapping();
        IntrospectionContext context = new DefaultIntrospectionContext(null, null, null, null, mapping);

        processor.visitField(annotation, field, new TestImplementation(), context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof InvalidAccessor);
    }

    public static class TestPrivateClass {
        @Property
        private void setProperty(String clazz) {

        }

        @Property(required = true)
        private void setRequiredProperty(String clazz) {

        }

        @Property
        private String property;

        @Property(required = true)
        private String requiredProperty;
    }

    public static class TestImplementation extends Implementation {
        private static final long serialVersionUID = 2759280710238779821L;

        public QName getType() {
            return null;
        }

        public AbstractComponentType getComponentType() {
            return new InjectingComponentType();
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        processor = new OASISPropertyProcessor<Implementation<? extends InjectingComponentType>>(helper);


    }
}