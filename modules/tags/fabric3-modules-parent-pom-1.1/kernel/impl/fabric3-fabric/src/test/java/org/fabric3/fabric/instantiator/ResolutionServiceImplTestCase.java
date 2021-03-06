/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.instantiator;

import java.net.URI;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.fabric.instantiator.promotion.DefaultPromotionResolutionService;
import org.fabric3.fabric.instantiator.target.ExplicitTargetResolutionService;
import org.fabric3.fabric.instantiator.target.ServiceContractResolver;
import org.fabric3.fabric.instantiator.target.ServiceContractResolverImpl;
import org.fabric3.fabric.instantiator.target.TypeBasedAutowireResolutionService;
import org.fabric3.model.type.component.AbstractComponentType;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ResourceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.service.JavaServiceContract;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * @version $Rev: 2852 $ $Date: 2008-02-21 23:24:40 +0000 (Thu, 21 Feb 2008) $
 */
public class ResolutionServiceImplTestCase extends TestCase {
    private static final URI REFERENCE_URI = URI.create("source#ref");
    private static final URI SOURCE_URI = URI.create("source");
    private static final URI TARGET_URI = URI.create("target#service");
    private LogicalCompositeComponent domain;
    private ResolutionServiceImpl resolutionService;

    public void testAutowireAtomicToAtomic() throws Exception {
        LogicalCompositeComponent composite = createWiredComposite(domain, Foo.class, Foo.class);
        InstantiationContext context = new InstantiationContext(domain);
        resolutionService.resolve(composite, context);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        assertEquals(TARGET_URI, source.getReference("ref").getWires().iterator().next().getTargetUri());
    }

    public void testAutowireAtomicToAtomicRequiresSuperInterface() throws Exception {
        LogicalCompositeComponent composite = createWiredComposite(domain, SuperFoo.class, Foo.class);
        InstantiationContext context = new InstantiationContext(domain);
        resolutionService.resolve(composite, context);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        resolutionService.resolve(composite, context);
        assertEquals(TARGET_URI, source.getReference("ref").getWires().iterator().next().getTargetUri());
    }

    public void testAutowireAtomicToAtomicRequiresSubInterface() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(domain, Foo.class, SuperFoo.class);
        InstantiationContext context = new InstantiationContext(domain);
        resolutionService.resolve(composite, context);
        assertTrue(context.getErrors().get(0) instanceof ReferenceNotFound);
    }

    public void testAutowireAtomicToAtomicIncompatibleInterfaces() throws Exception {
        LogicalComponent<CompositeImplementation> composite = createWiredComposite(domain, Foo.class, String.class);
        InstantiationContext context = new InstantiationContext(domain);
        resolutionService.resolve(composite, context);
        assertTrue(context.getErrors().get(0) instanceof ReferenceNotFound);
    }

    public void testNestedAutowireAtomicToAtomic() throws Exception {
        LogicalCompositeComponent composite = createWiredComposite(domain, Foo.class, Foo.class);
        LogicalCompositeComponent parent = createComposite("parent", composite);
        parent.addComponent(composite);
        parent.getDefinition().getImplementation().getComponentType().add(composite.getDefinition());
        InstantiationContext context = new InstantiationContext(domain);
        resolutionService.resolve(parent, context);
        LogicalComponent<?> source = composite.getComponent(SOURCE_URI);
        assertEquals(TARGET_URI, source.getReference("ref").getWires().iterator().next().getTargetUri());
    }

    public void testAutowireIncludeInComposite() throws Exception {
        LogicalCompositeComponent composite = createComposite("composite", domain);
        LogicalComponent<?> source = createSourceAtomic(Foo.class, composite);
        composite.addComponent(source);
        LogicalComponent<?> target = createTargetAtomic(Foo.class, composite);
        composite.addComponent(target);
        InstantiationContext context = new InstantiationContext(domain);
        resolutionService.resolve(source, context);
    }

    public void testAutowireToSiblingIncludeInComposite() throws Exception {
        LogicalCompositeComponent parent = createComposite("parent", null);
        LogicalCompositeComponent composite = createComposite("composite", parent);
        LogicalComponent<?> source = createSourceAtomic(Foo.class, composite);
        composite.addComponent(source);
        LogicalComponent<?> target = createTargetAtomic(Foo.class, composite);
        parent.addComponent(composite);
        composite.addComponent(target);
        InstantiationContext context = new InstantiationContext(domain);
        resolutionService.resolve(composite, context);
    }


    protected void setUp() throws Exception {
        super.setUp();
        PromotionResolutionService promotionResolutionService = new DefaultPromotionResolutionService();
        ServiceContractResolver serviceContractResolver = new ServiceContractResolverImpl();
        ExplicitTargetResolutionService resolutionService = new ExplicitTargetResolutionService(serviceContractResolver);
        TypeBasedAutowireResolutionService autowireResolutionService = new TypeBasedAutowireResolutionService(serviceContractResolver);
        this.resolutionService = new ResolutionServiceImpl(promotionResolutionService, resolutionService, autowireResolutionService);
        URI domainUri = URI.create("fabric3://runtime");
        domain = new LogicalCompositeComponent(domainUri, null, null);
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
        return new LogicalCompositeComponent(parentUri, definition, parent);
    }

    private LogicalComponent<?> createSourceAtomic(Class<?> requiredInterface, LogicalCompositeComponent parent) {

        ServiceContract contract = new JavaServiceContract(requiredInterface);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("ref", contract, Multiplicity.ONE_ONE);
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
        LogicalComponent<?> component =
                new LogicalComponent<MockAtomicImpl>(SOURCE_URI, definition, parent);
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
        LogicalComponent component = new LogicalComponent<MockAtomicImpl>(uri, definition, parent);
        LogicalService logicalService = new LogicalService(TARGET_URI, service, parent);
        component.addService(logicalService);
        return component;
    }

    private class MockAtomicImpl extends Implementation<MockComponentType> {
        public QName getType() {
            throw new UnsupportedOperationException();
        }
    }

    private class MockComponentType extends AbstractComponentType<ServiceDefinition, ReferenceDefinition, Property, ResourceDefinition> {

    }

    private interface SuperFoo {

    }

    private interface Foo extends SuperFoo {

    }

}
