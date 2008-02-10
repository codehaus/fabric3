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

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.introspection.impl.contract.JavaServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class DefaultWireResolverTestCase extends TestCase {
    private static final URI REFERENCE_URI = URI.create("source#ref");
    private static final URI SOURCE_URI = URI.create("source");
    private static final URI TARGET_URI = URI.create("target#service");
    private LogicalCompositeComponent domain;
    private DefaultWireResolver resolver;

    public void testAutowireAtomicToAtomic() throws Exception {
        LogicalCompositeComponent composite = createWiredComposite(domain, Foo.class, Foo.class);
        resolver.resolve(composite);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        assertEquals(TARGET_URI, source.getReference("ref").getTargetUris().get(0));
    }

    public void testAutowireAtomicToAtomicRequiresSuperInterface() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(domain, SuperFoo.class, Foo.class);
        resolver.resolve(composite);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        resolver.resolve(composite);
        assertEquals(TARGET_URI, source.getReference("ref").getTargetUris().get(0));
    }

    public void testAutowireAtomicToAtomicRequiresSubInterface() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(domain, Foo.class, SuperFoo.class);
        try {
            resolver.resolve(composite);
            fail();
        } catch (AutowireTargetNotFoundException e) {
            // expected
        }
    }

    public void testAutowireAtomicToAtomicIncompatibleInterfaces() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(domain, Foo.class, String.class);
        try {
            resolver.resolve(composite);
            fail();
        } catch (AutowireTargetNotFoundException e) {
            // expected
        }
    }

    public void testNestedAutowireAtomicToAtomic() throws Exception {
        LogicalCompositeComponent composite = createWiredComposite(domain, Foo.class, Foo.class);
        LogicalCompositeComponent parent = createComposite("parent", composite);
        parent.addComponent(composite);
        parent.getDefinition().getImplementation().getComponentType().add(composite.getDefinition());
        resolver.resolve(parent);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        assertEquals(TARGET_URI, source.getReference("ref").getTargetUris().get(0));
    }

    public void testAutowireIncludeInComposite() throws Exception {
        LogicalCompositeComponent composite = createComposite("composite", domain);
        LogicalComponent<?> source = createSourceAtomic(Foo.class, composite);
        composite.addComponent(source);
        LogicalComponent<?> target = createTargetAtomic(Foo.class, composite);
        composite.addComponent(target);
        resolver.resolve(source);
    }

    public void testAutowireToSiblingIncludeInComposite() throws Exception {
        LogicalCompositeComponent parent = createComposite("parent", null);
        LogicalCompositeComponent composite = createComposite("composite", parent);
        LogicalComponent<?> source = createSourceAtomic(Foo.class, composite);
        composite.addComponent(source);
        LogicalComponent<?> target = createTargetAtomic(Foo.class, composite);
        parent.addComponent(composite);
        composite.addComponent(target);
        resolver.resolve(composite);
    }


    protected void setUp() throws Exception {
        super.setUp();
        resolver = new DefaultWireResolver();
        URI domainUri = URI.create("fabric3://./runtime");
        URI runtimeUri = URI.create("runtime");
        domain = new LogicalCompositeComponent(domainUri, runtimeUri, null, null);
    }

    private LogicalCompositeComponent createWiredComposite(LogicalCompositeComponent parent,
                                                                           Class<?> sourceClass,
                                                                           Class<?> targetClass) {
        LogicalCompositeComponent composite = createComposite("composite", parent);
        LogicalComponent<?> source = createSourceAtomic(sourceClass, composite);
        composite.addComponent(source);
        Composite type = composite.getDefinition().getImplementation().getComponentType();
        type.add(source.getDefinition());
        LogicalComponent<?> target = createTargetAtomic(targetClass, composite);
        composite.addComponent(target);
        type.add(target.getDefinition());
        return composite;
    }

    private LogicalCompositeComponent createComposite(String uri, LogicalCompositeComponent parent) {
        URI parentUri = URI.create(uri);
        Composite type = new Composite(null);
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>(parentUri.toString());
        definition.setImplementation(impl);
        URI id = URI.create("runtime");
        return new LogicalCompositeComponent(parentUri, id, definition, parent);
    }

    private LogicalComponent<?> createSourceAtomic(Class<?> requiredInterface, LogicalCompositeComponent parent) {

        ServiceContract contract = new JavaServiceContract(requiredInterface);
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
        LogicalComponent<?> component =
                new LogicalComponent<MockAtomicImpl>(SOURCE_URI, id, definition, parent);
        LogicalReference logicalReference = new LogicalReference(REFERENCE_URI, referenceDefinition, component);
        component.addReference(logicalReference);
        return component;
    }

    private LogicalComponent<?> createTargetAtomic(Class<?> serviceInterface, LogicalCompositeComponent parent) {
        URI uri = URI.create("target");
        JavaServiceContract contract = new JavaServiceContract(serviceInterface);
        ServiceDefinition service = new ServiceDefinition("service", contract);
        MockComponentType type = new MockComponentType();
        type.add(service);
        MockAtomicImpl impl = new MockAtomicImpl();
        impl.setComponentType(type);
        ComponentDefinition<MockAtomicImpl> definition = new ComponentDefinition<MockAtomicImpl>(uri.toString());
        definition.setImplementation(impl);
        URI id = URI.create("runtime");
        return new LogicalComponent<MockAtomicImpl>(uri, id, definition, parent);
    }

    private class MockAtomicImpl extends Implementation<MockComponentType> {
        public QName getType() {
            throw new UnsupportedOperationException();
        }
    }

    private class MockComponentType extends AbstractComponentType<ServiceDefinition, ReferenceDefinition, Property<?>, ResourceDefinition> {

    }

    private interface SuperFoo {

    }

    private interface Foo extends SuperFoo {

    }

}
