/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.contract.ContractProcessor;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.InjectingComponentType;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.scdl.AbstractComponentType;

@SuppressWarnings("unchecked")
public class ReferenceProcessorTestCase extends TestCase {
    private ReferenceProcessor<Implementation<? extends InjectingComponentType>> processor;

    public void testInvalidMethodAccessor() throws Exception {
        Method method = TestPrivateClass.class.getDeclaredMethod("setRequiredReference", TestPrivateClass.class);
        Reference annotation = method.getAnnotation(Reference.class);
        TypeMapping mapping = new TypeMapping();
        IntrospectionContext context = new DefaultIntrospectionContext(null, null, null, null, mapping);

        processor.visitMethod(annotation, method, new TestImplementation(), context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof InvalidAccessor);
    }

    public void testWarningMethodAccessor() throws Exception {
        Method method = TestPrivateClass.class.getDeclaredMethod("setReference", TestPrivateClass.class);
        Reference annotation = method.getAnnotation(Reference.class);
        TypeMapping mapping = new TypeMapping();
        IntrospectionContext context = new DefaultIntrospectionContext(null, null, null, null, mapping);

        processor.visitMethod(annotation, method, new TestImplementation(), context);
        assertEquals(1, context.getWarnings().size());
        assertTrue(context.getWarnings().get(0) instanceof InvalidAccessor);
    }

    public void testInvalidFieldAccessor() throws Exception {
        Field field = TestPrivateClass.class.getDeclaredField("requiredFieldReference");
        Reference annotation = field.getAnnotation(Reference.class);
        TypeMapping mapping = new TypeMapping();
        IntrospectionContext context = new DefaultIntrospectionContext(null, null, null, null, mapping);

        processor.visitField(annotation, field, new TestImplementation(), context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof InvalidAccessor);
    }

    public void testWarningFieldAccessor() throws Exception {
        Field field = TestPrivateClass.class.getDeclaredField("fieldReference");
        Reference annotation = field.getAnnotation(Reference.class);
        TypeMapping mapping = new TypeMapping();
        IntrospectionContext context = new DefaultIntrospectionContext(null, null, null, null, mapping);

        processor.visitField(annotation, field, new TestImplementation(), context);
        assertEquals(1, context.getWarnings().size());
        assertTrue(context.getWarnings().get(0) instanceof InvalidAccessor);
    }


    public static class TestPrivateClass {
        @Reference
        private void setRequiredReference(TestPrivateClass clazz) {

        }

        @Reference(required = false)
        private void setReference(TestPrivateClass clazz) {

        }

        @Reference
        private TestPrivateClass requiredFieldReference;

        @Reference(required = false)
        private TestPrivateClass fieldReference;

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
        final ServiceContract<Type> contract = new ServiceContract<Type>() {
            private static final long serialVersionUID = -1453983556738324512L;

            public boolean isAssignableFrom(ServiceContract serviceContract) {
                return false;
            }

            public String getQualifiedInterfaceName() {
                return null;
            }
        };

        ContractProcessor contractProcessor = new ContractProcessor() {

            public ServiceContract<Type> introspect(TypeMapping typeMapping, Type type, ValidationContext context) {
                return contract;
            }
        };
        processor = new ReferenceProcessor<Implementation<? extends InjectingComponentType>>(contractProcessor, helper);

    }
}