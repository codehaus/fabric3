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
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.ComponentType;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.model.type.Implementation;
import org.fabric3.spi.model.type.Property;
import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.ReferenceTarget;
import org.fabric3.spi.model.type.ServiceContract;
import org.fabric3.spi.model.type.ServiceDefinition;

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
        resolver.resolve(domain, composite);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        assertEquals(TARGET_URI, source.getReference("ref").getTargetUris().get(0));
    }

    public void testAutowireAtomicToAtomicRequiresSuperInterface() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(SuperFoo.class, Foo.class);
        resolver.resolve(domain, composite);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        resolver.resolve(domain, composite);
        assertEquals(TARGET_URI, source.getReference("ref").getTargetUris().get(0));
    }

    public void testAutowireAtomicToAtomicRequiresSubInterface() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(Foo.class, SuperFoo.class);
        try {
            resolver.resolve(domain, composite);
            fail();
        } catch (AutowireTargetNotFoundException e) {
            // expected
        }
    }

    public void testAutowireAtomicToAtomicIncompatibleInterfaces() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(Foo.class, String.class);
        try {
            resolver.resolve(domain, composite);
            fail();
        } catch (AutowireTargetNotFoundException e) {
            // expected
        }
    }

    public void testNestedAutowireAtomicToAtomic() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(Foo.class, Foo.class);
        LogicalComponent<CompositeImplementation> parent = createComposite("parent");
        parent.addComponent(composite);
        parent.getDefinition().getImplementation().getComponentType().add(composite.getDefinition());
        resolver.resolve(domain, parent);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        assertEquals(TARGET_URI, source.getReference("ref").getTargetUris().get(0));
    }


    protected void setUp() throws Exception {
        super.setUp();
        resolver = new DefaultWireResolver();
        URI domainUri = URI.create("fabric3://./runtime");
        URI runtimeUri = URI.create("runtime");
        domain = new LogicalComponent<CompositeImplementation>(domainUri, runtimeUri, null);
    }

    private LogicalComponent<CompositeImplementation> createWiredComposite(Class<?> sourceClass, Class<?> targetClass) {
        LogicalComponent<CompositeImplementation> composite = createComposite("composite");
        LogicalComponent<?> source = createSourceAtomic(sourceClass);
        composite.addComponent(source);
        CompositeComponentType type = composite.getDefinition().getImplementation().getComponentType();
        type.add(source.getDefinition());
        LogicalComponent<?> target = createTargetAtomic(targetClass);
        composite.addComponent(target);
        type.add(target.getDefinition());
        return composite;
    }

    private LogicalComponent<CompositeImplementation> createComposite(String uri) {
        URI parentUri = URI.create(uri);
        CompositeComponentType type = new CompositeComponentType();
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>(parentUri.toString(), impl);
        URI id = URI.create("runtime");
        return new LogicalComponent<CompositeImplementation>(parentUri, id, definition);
    }

    private LogicalComponent<?> createSourceAtomic(Class<?> requiredInterface) {

        ServiceContract contract = new ServiceContract() {
        };
        contract.setInterfaceClass(requiredInterface);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition(URI.create("#ref"), contract);
        referenceDefinition.setRequired(true);
        MockComponentType type = new MockComponentType();
        type.add(referenceDefinition);
        MockAtomicImpl impl = new MockAtomicImpl();
        impl.setComponentType(type);
        ComponentDefinition<MockAtomicImpl> definition =
                new ComponentDefinition<MockAtomicImpl>(SOURCE_URI.toString(), impl);
        ReferenceTarget target = new ReferenceTarget();
        target.setReferenceName(REFERENCE_URI);
        target.setAutowire(true);
        definition.add(target);
        URI id = URI.create("runtime");
        LogicalComponent<?> component = new LogicalComponent<MockAtomicImpl>(SOURCE_URI, id, definition);
        LogicalReference logicalReference = new LogicalReference(REFERENCE_URI, referenceDefinition);
        component.addReference(logicalReference);
        return component;
    }

    private LogicalComponent<?> createTargetAtomic(Class<?> serviceInterface) {
        URI uri = URI.create("target");
        ServiceDefinition service = new ServiceDefinition();
        service.setUri(URI.create("#service"));
        ServiceContract contract = new ServiceContract() {
        };
        contract.setInterfaceClass(serviceInterface);
        service.setServiceContract(contract);
        MockComponentType type = new MockComponentType();
        type.add(service);
        MockAtomicImpl impl = new MockAtomicImpl();
        impl.setComponentType(type);
        ComponentDefinition<MockAtomicImpl> definition = new ComponentDefinition<MockAtomicImpl>(uri.toString(), impl);
        URI id = URI.create("runtime");
        return new LogicalComponent<MockAtomicImpl>(uri, id, definition);
    }

    private class MockAtomicImpl extends Implementation<MockComponentType> {

    }

    private class MockComponentType extends ComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> {

    }

    private interface SuperFoo {

    }

    private interface Foo extends SuperFoo {

    }

}
