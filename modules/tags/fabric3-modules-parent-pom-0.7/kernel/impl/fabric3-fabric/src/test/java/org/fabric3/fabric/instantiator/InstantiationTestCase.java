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
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import org.fabric3.fabric.instantiator.component.AtomicComponentInstantiator;
import org.fabric3.fabric.instantiator.component.CompositeComponentInstantiator;
import org.fabric3.fabric.instantiator.WireInstantiator;
import org.fabric3.fabric.instantiator.component.WireInstantiatorImpl;
import org.fabric3.fabric.instantiator.PromotionNormalizer;
import org.fabric3.model.type.component.AbstractComponentType;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.CompositeReference;
import org.fabric3.model.type.component.CompositeService;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * @version $Rev$ $Date$
 */
public class InstantiationTestCase extends TestCase {
    public static final URI PARENT_URI = URI.create("fabric3://domain/parent");
    public static final URI COMPONENT_BASE = URI.create("fabric3://domain/parent/component");
    public static final String COMPONENT_URI = PARENT_URI.toString() + "/component";
    public static final String CHILD_URI = COMPONENT_URI + "/child";
    public static final String SERVICE_URI = COMPONENT_URI + "#service";
    public static final String REFERENCE_URI = COMPONENT_URI + "#reference";

    private LogicalModelInstantiator logicalModelInstantiator;
    private LogicalCompositeComponent parent;

    public void testInstantiateChildren() throws Exception {
        ComponentDefinition<?> definition = createParentWithChild();
        Composite composite = new Composite(null);
        composite.add(definition);
        logicalModelInstantiator.include(parent, composite);
        LogicalCompositeComponent logicalComponent = (LogicalCompositeComponent) parent.getComponents().iterator().next();
        assertEquals(COMPONENT_URI, logicalComponent.getUri().toString());
        LogicalComponent<?> logicalChild = logicalComponent.getComponent(URI.create(CHILD_URI));
        assertEquals(CHILD_URI, logicalChild.getUri().toString());
    }

    public void testInstantiateServiceReference() throws Exception {
        ComponentDefinition<?> definition = createParentWithServiceAndReference();
        Composite composite = new Composite(null);
        composite.add(definition);
        logicalModelInstantiator.include(parent, composite);
        LogicalCompositeComponent logicalComponent = (LogicalCompositeComponent) parent.getComponents().iterator().next();
        LogicalService logicalService = logicalComponent.getService("service");
        assertEquals(SERVICE_URI, logicalService.getUri().toString());
        LogicalReference logicalReference = logicalComponent.getReference("reference");
        assertEquals(REFERENCE_URI, logicalReference.getUri().toString());
    }


    protected void setUp() throws Exception {
        super.setUp();

        AtomicComponentInstantiator atomicComponentInstantiator = new AtomicComponentInstantiator(null);
        WireInstantiator wireInstantiator = new WireInstantiatorImpl();
        CompositeComponentInstantiator compositeComponentInstantiator =
                new CompositeComponentInstantiator(atomicComponentInstantiator, wireInstantiator, null);
        ResolutionService resolutionService = EasyMock.createMock(ResolutionService.class);
        PromotionNormalizer normalizer = EasyMock.createMock(PromotionNormalizer.class);

        logicalModelInstantiator =
                new LogicalModelInstantiatorImpl(resolutionService,
                                                 normalizer,
                                                 null,
                                                 atomicComponentInstantiator,
                                                 compositeComponentInstantiator,
                                                 wireInstantiator);
        parent = new LogicalCompositeComponent(PARENT_URI, null, null);
    }

    private ComponentDefinition<?> createParentWithChild() {
        ComponentType childType = new ComponentType();
        MockImplementation childImp = new MockImplementation();
        childImp.setComponentType(childType);
        ComponentDefinition<MockImplementation> child =
                new ComponentDefinition<MockImplementation>("child");
        child.setImplementation(childImp);

        Composite type = new Composite(null);
        type.add(child);
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>("component");
        definition.setImplementation(implementation);
        return definition;

    }

    private ComponentDefinition<?> createParentWithServiceAndReference() {
        CompositeService service = new CompositeService("service", null, null);
        List<URI> references = Collections.emptyList();
        CompositeReference reference = new CompositeReference("reference", references);
        Composite type = new Composite(null);
        type.add(service);
        type.add(reference);
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>("component");
        definition.setImplementation(implementation);
        return definition;

    }

    private class MockImplementation extends Implementation<AbstractComponentType<?, ?, ?, ?>> {
        public QName getType() {
            return null;
        }
    }

}
