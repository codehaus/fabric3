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

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Remotable;
import org.osoa.sca.annotations.Service;

import org.fabric3.fabric.idl.java.JavaInterfaceProcessorRegistryImpl;
import org.fabric3.pojo.scdl.ConstructorDefinition;
import org.fabric3.pojo.scdl.JavaMappedProperty;
import org.fabric3.pojo.scdl.JavaMappedReference;
import org.fabric3.pojo.scdl.JavaMappedService;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.spi.idl.java.JavaServiceContract;

/**
 * Verfies component type information is properly introspected from an unadorned POJO according to the SCA Java Client
 * and Implementation Model Specification
 *
 * @version $Rev$ $Date$
 */
public class HeuristicPojoProcessorTestCase extends TestCase {

    private HeuristicPojoProcessor processor =
            new HeuristicPojoProcessor(new ImplementationProcessorServiceImpl(new JavaInterfaceProcessorRegistryImpl()),
                                       null);

    /**
     * Verifies a single service interface is computed when only one interface is implemented
     */
    public void testSingleInterface() throws Exception {
        PojoComponentType type =  new PojoComponentType(null);
        Constructor<SingleInterfaceImpl> ctor = SingleInterfaceImpl.class.getConstructor();
        type.setConstructorDefinition(new ConstructorDefinition<SingleInterfaceImpl>(ctor));
        processor.visitEnd(SingleInterfaceImpl.class, type, null);
        assertEquals(1, type.getServices().size());
        Map<String, JavaMappedService> services = type.getServices();
        JavaMappedService mappedService = services.get(PropertyInterface.class.getSimpleName());
        JavaServiceContract contract = JavaServiceContract.class.cast(mappedService.getServiceContract());
        assertEquals(PropertyInterface.class.getName(), contract.getInterfaceClass());
        assertTrue(type.getProperties().isEmpty());
        assertTrue(type.getReferences().isEmpty());
    }

    /**
     * Verifies property and reference setters are computed
     */
    public void testPropertyReference() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<SingleInterfaceWithPropertyReferenceImpl> ctor =
                SingleInterfaceWithPropertyReferenceImpl.class.getConstructor();
        type.setConstructorDefinition(new ConstructorDefinition<SingleInterfaceWithPropertyReferenceImpl>(ctor));
        processor.visitEnd(SingleInterfaceWithPropertyReferenceImpl.class, type, null);
        assertEquals(1, type.getServices().size());
        Map<String, JavaMappedService> services = type.getServices();
        JavaMappedService mappedService = services.get(Interface1.class.getSimpleName());
        JavaServiceContract contract = JavaServiceContract.class.cast(mappedService.getServiceContract());
        assertEquals(Interface1.class.getName(), contract.getInterfaceClass());
        assertEquals(1, type.getProperties().size());
        assertEquals(ComplexProperty.class, type.getProperties().get("property").getJavaType());
        assertEquals(1, type.getReferences().size());
        Map<String, JavaMappedReference> references = type.getReferences();
        JavaMappedReference mappedReference = references.get("reference");
        JavaServiceContract refContract = JavaServiceContract.class.cast(mappedReference.getServiceContract());
        assertEquals(Ref.class.getName(), refContract.getInterfaceClass());
    }

    /**
     * Verifies that a property setter is not introspected if an analogous operation is in the service interface
     */
    public void testPropertySetterInInterface() throws Exception {
        PojoComponentType type =
                new PojoComponentType(null);
        Constructor<SingleInterfaceImpl> ctor = SingleInterfaceImpl.class.getConstructor();
        type.setConstructorDefinition(new ConstructorDefinition<SingleInterfaceImpl>(ctor));
        processor.visitEnd(SingleInterfaceImpl.class, type, null);
        assertEquals(0, type.getProperties().size());
    }

    /**
     * Verifies that a reference setter is not introspected if an analogous operation is in the service interface
     */
    public void testReferenceSetterInInterface() throws Exception {
        PojoComponentType type =
                new PojoComponentType(null);
        Constructor<RefInterfaceImpl> ctor = RefInterfaceImpl.class.getConstructor();
        type.setConstructorDefinition(new ConstructorDefinition<RefInterfaceImpl>(ctor));
        processor.visitEnd(RefInterfaceImpl.class, type, null);
        assertEquals(0, type.getReferences().size());
    }

    /**
     * Verifies collection generic types or array types are introspected as references according to spec rules
     */
    public void testReferenceCollectionType() throws Exception {
        PojoComponentType type =
                new PojoComponentType(null);
        Constructor<ReferenceCollectionImpl> ctor = ReferenceCollectionImpl.class.getConstructor();
        type.setConstructorDefinition(new ConstructorDefinition<ReferenceCollectionImpl>(ctor));
        processor.visitEnd(ReferenceCollectionImpl.class, type, null);
        assertEquals(0, type.getProperties().size());
        assertEquals(4, type.getReferences().size());
    }

    /**
     * Verifies collection generic types or array types are introspected as properties according to spec rules
     */
    public void testPropertyCollectionType() throws Exception {
        PojoComponentType type =
                new PojoComponentType(null);
        Constructor<PropertyCollectionImpl> ctor = PropertyCollectionImpl.class.getConstructor();
        type.setConstructorDefinition(new ConstructorDefinition<PropertyCollectionImpl>(ctor));
        processor.visitEnd(PropertyCollectionImpl.class, type, null);
        assertEquals(0, type.getReferences().size());
        assertEquals(4, type.getProperties().size());
    }

    /**
     * Verifies references are calculated when the type marked with is @Remotable
     */
    public void testRemotableRef() throws Exception {
        PojoComponentType type =
                new PojoComponentType(null);
        Constructor<RemotableRefImpl> ctor = RemotableRefImpl.class.getConstructor();
        type.setConstructorDefinition(new ConstructorDefinition<RemotableRefImpl>(ctor));
        processor.visitEnd(RemotableRefImpl.class, type, null);
        assertEquals(2, type.getReferences().size());
        assertEquals(0, type.getProperties().size());
    }

    public void testParentInterface() throws ProcessingException, NoSuchMethodException {
        PojoComponentType type =
                new PojoComponentType(null);
        Constructor<Child> ctor = Child.class.getConstructor();
        type.setConstructorDefinition(new ConstructorDefinition<Child>(ctor));
        processor.visitEnd(Child.class, type, null);
        assertTrue(type.getServices().containsKey(Interface1.class.getSimpleName()));
    }

    /**
     * Verifies a service inteface is calculated when only props and refs are given
     */
    public void testExcludedPropertyAndReference() throws Exception {
        PojoComponentType type =
                new PojoComponentType(null);
        JavaMappedReference ref = new JavaMappedReference("reference", null, null);
        type.add(ref);
        JavaMappedReference ref2 = new JavaMappedReference("reference2", null, null);
        type.add(ref2);
        JavaMappedProperty<?> prop1 = new JavaMappedProperty();
        prop1.setName("string1");
        type.add(prop1);
        JavaMappedProperty<?> prop2 = new JavaMappedProperty();
        prop2.setName("string2");
        type.add(prop2);
        processor.visitEnd(MockService.class, type, null);
        assertEquals(1, type.getServices().size());
    }

    public void testProtectedRemotableRefField() throws ProcessingException, NoSuchMethodException {
        PojoComponentType type =
                new PojoComponentType(null);
        Constructor<ProtectedRemotableRefFieldImpl> ctor = ProtectedRemotableRefFieldImpl.class.getConstructor();
        type.setConstructorDefinition(new ConstructorDefinition<ProtectedRemotableRefFieldImpl>(ctor));
        processor.visitEnd(ProtectedRemotableRefFieldImpl.class, type, null);
        assertNotNull(type.getReferences().get("otherRef"));
    }

    public void testProtectedRemotableRefMethod() throws ProcessingException, NoSuchMethodException {
        PojoComponentType type =
                new PojoComponentType(null);
        Constructor<ProtectedRemotableRefMethodImpl> ctor = ProtectedRemotableRefMethodImpl.class.getConstructor();
        type.setConstructorDefinition(new ConstructorDefinition<ProtectedRemotableRefMethodImpl>(ctor));
        processor.visitEnd(ProtectedRemotableRefMethodImpl.class, type, null);
        assertNotNull(type.getReferences().get("otherRef"));
    }

    public void testSetDataTypes() throws Exception {
        PojoComponentType type =
                new PojoComponentType(null);
        Constructor<PropertyIntTypeOnConstructor> ctor = PropertyIntTypeOnConstructor.class.getConstructor(int.class);
        type.setConstructorDefinition(new ConstructorDefinition<PropertyIntTypeOnConstructor>(ctor));
        processor.visitEnd(ProtectedRemotableRefMethodImpl.class, type, null);
        org.fabric3.scdl.Property<?> foo = type.getProperties().get("foo");
        assertEquals(int.class, foo.getJavaType());
    }

    private static class PropertyIntTypeOnConstructor {
        private int foo;

        public PropertyIntTypeOnConstructor(@Property(name = "foo")int foo) {
            this.foo = foo;
        }

        public int getFoo() {
            return foo;
        }
    }

    private interface PropertyInterface {
        void setString1(String val);
    }

    private interface Interface1 {
    }

    private static class Parent implements Interface1 {

    }

    private static class Child extends Parent {
        public Child() {
        }

    }

    private static class SingleInterfaceImpl implements PropertyInterface {
        public SingleInterfaceImpl() {
        }

        public void setString1(String val) {
        }

    }

    private interface HeuristicServiceInterface {
        void fooOperation(String ref);

        void setInvalid1(); // No parameter

        void setInvalid2(String str, int i); // More than one parameter

        String setInvalid3(String str); // return should be void
    }

    public static class MockService implements PropertyInterface, RefInterface, HeuristicServiceInterface {

        @Property
        public void setString1(String val) {
        }

        @Property
        public void setString2(String val) {
        }

        @Reference
        public void setReference(Ref ref) {
        }

        @Reference
        public void setReference2(Ref ref) {
        }

        public void fooOperation(String ref) {

        }

        public void setInvalid1() {
        }

        public void setInvalid2(String str, int i) {
        }

        public String setInvalid3(String str) {
            return null;
        }

    }

    @Service
    private interface Ref {
    }

    private class ComplexProperty {
    }

    private interface RefInterface {
        void setReference(Ref ref);
    }

    private static class RefInterfaceImpl implements RefInterface {
        public RefInterfaceImpl() {
        }

        public void setReference(Ref ref) {
        }
    }

    private static class SingleInterfaceWithPropertyReferenceImpl implements Interface1 {
        public SingleInterfaceWithPropertyReferenceImpl() {
        }

        public void setReference(Ref ref) {
        }

        public void setProperty(ComplexProperty prop) {
        }
    }

    private static class ReferenceCollectionImpl implements Interface1 {
        public ReferenceCollectionImpl() {
        }

        public void setCollectionReference(Collection<Ref> ref) {
        }

        public void setNonGenericCollectionReference(Collection ref) {
        }

        public void setListReference(List<Ref> ref) {
        }

        public void setArrayReference(Ref[] ref) {
        }
    }

    private static class PropertyCollectionImpl implements Interface1 {
        public PropertyCollectionImpl() {
        }

        public void setCollectionProperty(Collection<ComplexProperty> prop) {
        }

        public void setCollectionProperty2(Collection<String> prop) {
        }

        public void setArrayProperty(ComplexProperty[] prop) {
        }

        public void setArrayProperty2(String[] prop) {
        }
    }

    @Remotable
    private interface RemotableRef {
    }

    private static class RemotableRefImpl implements Interface1 {
        protected RemotableRef otherRef;

        public RemotableRefImpl() {
        }

        public void setRef(RemotableRef ref) {

        }
    }

    private static class ProtectedRemotableRefFieldImpl implements Interface1 {
        protected RemotableRef otherRef;

        public ProtectedRemotableRefFieldImpl() {
        }

        public ProtectedRemotableRefFieldImpl(RemotableRef otherRef) {
            this.otherRef = otherRef;
        }

    }

    private static class ProtectedRemotableRefMethodImpl implements Interface1 {
        public ProtectedRemotableRefMethodImpl() {
        }

        protected void setOtherRef(RemotableRef otherRef) {
        }

    }


}
