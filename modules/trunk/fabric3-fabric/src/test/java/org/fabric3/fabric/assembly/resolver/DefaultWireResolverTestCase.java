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
package org.fabric3.fabric.assembly.resolver;

import java.net.URI;

import junit.framework.TestCase;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ComponentReference;

/**
 * @version $Rev$ $Date$
 */
public class DefaultWireResolverTestCase extends TestCase {
    private static final URI REFERENCE_URI = URI.create("source#ref");
    private static final URI SOURCE_URI = URI.create("source");
    private static final URI TARGET_URI = URI.create("target#service");
    private LogicalComponent<CompositeImplementation> domain;
    private DefaultWireResolver resolver;

    public void testAutowireAtomicToAtomic() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(Foo.class, Foo.class);
        resolver.resolve(domain, composite, false);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        assertEquals(TARGET_URI, source.getReference("ref").getTargetUris().get(0));
    }

    public void testAutowireAtomicToAtomicRequiresSuperInterface() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(SuperFoo.class, Foo.class);
        resolver.resolve(domain, composite, false);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        resolver.resolve(domain, composite, false);
        assertEquals(TARGET_URI, source.getReference("ref").getTargetUris().get(0));
    }

    public void testAutowireAtomicToAtomicRequiresSubInterface() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(Foo.class, SuperFoo.class);
        try {
            resolver.resolve(domain, composite, false);
            fail();
        } catch (AutowireTargetNotFoundException e) {
            // expected
        }
    }

    public void testAutowireAtomicToAtomicIncompatibleInterfaces() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(Foo.class, String.class);
        try {
            resolver.resolve(domain, composite, false);
            fail();
        } catch (AutowireTargetNotFoundException e) {
            // expected
        }
    }

    public void testNestedAutowireAtomicToAtomic() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(Foo.class, Foo.class);
        LogicalComponent<CompositeImplementation> parent = createComposite("parent", composite);
        parent.addComponent(composite);
        parent.getDefinition().getImplementation().getComponentType().add(composite.getDefinition());
        resolver.resolve(domain, parent, false);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        assertEquals(TARGET_URI, source.getReference("ref").getTargetUris().get(0));
    }

    public void testAutowireIncludeInComposite() throws Exception {
        LogicalComponent<CompositeImplementation> parent = createComposite("parent", null);
        LogicalComponent<CompositeImplementation> composite = createComposite("composite", parent);
        LogicalComponent<?> source = createSourceAtomic(Foo.class, composite);
        composite.addComponent(source);
        LogicalComponent<?> target = createTargetAtomic(Foo.class, composite);
        parent.addComponent(composite);
        parent.addComponent(target);
        resolver.resolve(parent, composite, true);
    }

    public void testAutowireToSiblingIncludeInComposite() throws Exception {
        LogicalComponent<CompositeImplementation> parent = createComposite("parent", null);
        LogicalComponent<CompositeImplementation> composite = createComposite("composite", parent);
        LogicalComponent<?> source = createSourceAtomic(Foo.class, composite);
        composite.addComponent(source);
        LogicalComponent<?> target = createTargetAtomic(Foo.class, composite);
        parent.addComponent(composite);
        composite.addComponent(target);
        resolver.resolve(parent, composite, true);
    }


    protected void setUp() throws Exception {
        super.setUp();
        resolver = new DefaultWireResolver();
        URI domainUri = URI.create("fabric3://./runtime");
        URI runtimeUri = URI.create("runtime");
        domain = new LogicalComponent<CompositeImplementation>(domainUri, runtimeUri, null, null, null);
    }

    private LogicalComponent<CompositeImplementation> createWiredComposite(Class<?> sourceClass, Class<?> targetClass) {
        LogicalComponent<CompositeImplementation> composite = createComposite("composite", null);
        LogicalComponent<?> source = createSourceAtomic(sourceClass, composite);
        composite.addComponent(source);
        Composite type = composite.getDefinition().getImplementation().getComponentType();
        type.add(source.getDefinition());
        LogicalComponent<?> target = createTargetAtomic(targetClass, composite);
        composite.addComponent(target);
        type.add(target.getDefinition());
        return composite;
    }

    private LogicalComponent<CompositeImplementation> createComposite(String uri, LogicalComponent<CompositeImplementation> parent) {
        URI parentUri = URI.create(uri);
        Composite type = new Composite(null);
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>(parentUri.toString());
        definition.setImplementation(impl);
        URI id = URI.create("runtime");
        return new LogicalComponent<CompositeImplementation>(parentUri, id, definition, parent, definition.getKey());
    }

    private LogicalComponent<?> createSourceAtomic(Class<?> requiredInterface, LogicalComponent<CompositeImplementation> parent) {

        ServiceContract contract = new ServiceContract() {
        };
        contract.setInterfaceClass(requiredInterface);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("ref", contract);
        referenceDefinition.setRequired(true);
        MockComponentType type = new MockComponentType();
        type.add(referenceDefinition);
        MockAtomicImpl impl = new MockAtomicImpl();
        impl.setComponentType(type);
        ComponentDefinition<MockAtomicImpl> definition =
                new ComponentDefinition<MockAtomicImpl>(SOURCE_URI.toString());
        definition.setImplementation(impl);
        ComponentReference target = new ComponentReference(REFERENCE_URI.getFragment());
        target.setAutowire(true);
        definition.add(target);
        URI id = URI.create("runtime");
        LogicalComponent<?> component = new LogicalComponent<MockAtomicImpl>(SOURCE_URI, id, definition, parent, definition.getKey());
        LogicalReference logicalReference = new LogicalReference(REFERENCE_URI, referenceDefinition, component);
        component.addReference(logicalReference);
        return component;
    }

    private LogicalComponent<?> createTargetAtomic(Class<?> serviceInterface, LogicalComponent<CompositeImplementation> parent) {
        URI uri = URI.create("target");
        ServiceContract contract = new ServiceContract() {
        };
        contract.setInterfaceClass(serviceInterface);
        ServiceDefinition service = new ServiceDefinition("service", contract);
        MockComponentType type = new MockComponentType();
        type.add(service);
        MockAtomicImpl impl = new MockAtomicImpl();
        impl.setComponentType(type);
        ComponentDefinition<MockAtomicImpl> definition = new ComponentDefinition<MockAtomicImpl>(uri.toString());
        definition.setImplementation(impl);
        URI id = URI.create("runtime");
        return new LogicalComponent<MockAtomicImpl>(uri, id, definition, parent, definition.getKey());
    }

    private class MockAtomicImpl extends Implementation<MockComponentType> {

    }

    private class MockComponentType extends AbstractComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> {

    }

    private interface SuperFoo {

    }

    private interface Foo extends SuperFoo {

    }

}
