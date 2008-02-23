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
import org.easymock.EasyMock;

import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.pojo.processor.ProcessingException;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.Signature;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.introspection.IntrospectionContext;

/**
 * Verfies component type information is properly introspected from an unadorned POJO according to the SCA Java Client
 * and Implementation Model Specification
 *
 * @version $Rev$ $Date$
 */
public class HeuristicPojoProcessorTestCase extends TestCase {

    private HeuristicPojoProcessor processor;
    private IntrospectionContext context;

    protected void setUp() throws Exception {
        super.setUp();
        DefaultIntrospectionHelper helper = new DefaultIntrospectionHelper();
        DefaultContractProcessor contractProcessor = new DefaultContractProcessor(helper);
        ImplementationProcessorServiceImpl processorService = new ImplementationProcessorServiceImpl(contractProcessor, helper);
        processor = new HeuristicPojoProcessor(processorService);

        TypeMapping typeMapping = new TypeMapping();
        context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.expect(context.getTypeMapping()).andStubReturn(typeMapping);
        EasyMock.replay(context);
    }

    /**
     * Verifies a single service interface is computed when only one interface is implemented
     */
    public void testSingleInterface() throws Exception {
        PojoComponentType type =  new PojoComponentType(null);
        Constructor<SingleInterfaceImpl> ctor = SingleInterfaceImpl.class.getConstructor();
        type.setConstructor(new Signature(ctor));
        processor.visitEnd(SingleInterfaceImpl.class, type, context);
        assertEquals(1, type.getServices().size());
        Map<String, ServiceDefinition> services = type.getServices();
        ServiceDefinition mappedService = services.get(PropertyInterface.class.getSimpleName());
        ServiceContract contract = mappedService.getServiceContract();
        assertEquals(PropertyInterface.class.getName(), contract.getQualifiedInterfaceName());
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
        type.setConstructor(new Signature(ctor));
        processor.visitEnd(SingleInterfaceWithPropertyReferenceImpl.class, type, context);
        assertEquals(1, type.getServices().size());
        Map<String, ServiceDefinition> services = type.getServices();
        ServiceDefinition mappedService = services.get(Interface1.class.getSimpleName());
        ServiceContract<?> contract = mappedService.getServiceContract();
        assertEquals(Interface1.class.getName(), contract.getQualifiedInterfaceName());
        assertEquals(1, type.getProperties().size());
        assertEquals(1, type.getReferences().size());
        Map<String, ReferenceDefinition> references = type.getReferences();
        ReferenceDefinition mappedReference = references.get("reference");
        ServiceContract refContract = mappedReference.getServiceContract();
        assertEquals(Ref.class.getName(), refContract.getQualifiedInterfaceName());
    }

    /**
     * Verifies that a property setter is not introspected if an analogous operation is in the service interface
     */
    public void testPropertySetterInInterface() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<SingleInterfaceImpl> ctor = SingleInterfaceImpl.class.getConstructor();
        type.setConstructor(new Signature(ctor));
        processor.visitEnd(SingleInterfaceImpl.class, type, context);
        assertEquals(0, type.getProperties().size());
    }

    /**
     * Verifies that a reference setter is not introspected if an analogous operation is in the service interface
     */
    public void testReferenceSetterInInterface() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<RefInterfaceImpl> ctor = RefInterfaceImpl.class.getConstructor();
        type.setConstructor(new Signature(ctor));
        processor.visitEnd(RefInterfaceImpl.class, type, context);
        assertEquals(0, type.getReferences().size());
    }

    /**
     * Verifies collection generic types or array types are introspected as references according to spec rules
     */
    public void testReferenceCollectionType() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<ReferenceCollectionImpl> ctor = ReferenceCollectionImpl.class.getConstructor();
        type.setConstructor(new Signature(ctor));
        processor.visitEnd(ReferenceCollectionImpl.class, type, context);
        assertEquals(0, type.getProperties().size());
        assertEquals(4, type.getReferences().size());
    }

    /**
     * Verifies collection generic types or array types are introspected as properties according to spec rules
     */
    public void testPropertyCollectionType() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<PropertyCollectionImpl> ctor = PropertyCollectionImpl.class.getConstructor();
        type.setConstructor(new Signature(ctor));
        processor.visitEnd(PropertyCollectionImpl.class, type, context);
        assertEquals(0, type.getReferences().size());
        assertEquals(4, type.getProperties().size());
    }

    /**
     * Verifies references are calculated when the type marked with is @Remotable
     */
    public void testRemotableRef() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<RemotableRefImpl> ctor = RemotableRefImpl.class.getConstructor();
        type.setConstructor(new Signature(ctor));
        processor.visitEnd(RemotableRefImpl.class, type, context);
        assertEquals(2, type.getReferences().size());
        assertEquals(0, type.getProperties().size());
    }

    public void testParentInterface() throws ProcessingException, NoSuchMethodException {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<Child> ctor = Child.class.getConstructor();
        type.setConstructor(new Signature(ctor));
        processor.visitEnd(Child.class, type, context);
        assertTrue(type.getServices().containsKey(Interface1.class.getSimpleName()));
    }

    /**
     * Verifies a service inteface is calculated when only props and refs are given
     */
    public void testExcludedPropertyAndReference() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        ReferenceDefinition ref = new ReferenceDefinition("reference", null, null);
        type.add(ref);
        ReferenceDefinition ref2 = new ReferenceDefinition("reference2", null, null);
        type.add(ref2);
        org.fabric3.scdl.Property prop1 = new org.fabric3.scdl.Property();
        prop1.setName("string1");
        type.add(prop1);
        org.fabric3.scdl.Property prop2 = new org.fabric3.scdl.Property();
        prop2.setName("string2");
        type.add(prop2);
        processor.visitEnd(MockService.class, type, context);
        assertEquals(1, type.getServices().size());
    }

    public void testProtectedRemotableRefField() throws ProcessingException, NoSuchMethodException {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<ProtectedRemotableRefFieldImpl> ctor = ProtectedRemotableRefFieldImpl.class.getConstructor();
        type.setConstructor(new Signature(ctor));
        processor.visitEnd(ProtectedRemotableRefFieldImpl.class, type, context);
        assertNotNull(type.getReferences().get("otherRef"));
    }

    public void testProtectedRemotableRefMethod() throws ProcessingException, NoSuchMethodException {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<ProtectedRemotableRefMethodImpl> ctor = ProtectedRemotableRefMethodImpl.class.getConstructor();
        type.setConstructor(new Signature(ctor));
        processor.visitEnd(ProtectedRemotableRefMethodImpl.class, type, context);
        assertNotNull(type.getReferences().get("otherRef"));
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
