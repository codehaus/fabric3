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
package org.fabric3.fabric.instantiator;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import org.fabric3.fabric.instantiator.normalize.PromotionNormalizer;
import org.fabric3.fabric.instantiator.component.AtomicComponentInstantiator;
import org.fabric3.fabric.instantiator.component.CompositeComponentInstantiator;
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentType;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.CompositeReference;
import org.fabric3.scdl.CompositeService;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * @version $Rev$ $Date$
 */
public class InstantiationTestCase extends TestCase {
    public static final URI PARENT_URI = URI.create("sca://./domain/parent");
    public static final URI COMPONENT_BASE = URI.create("sca://./domain/parent/component");
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
        CompositeComponentInstantiator compositeComponentInstantiator = new CompositeComponentInstantiator(atomicComponentInstantiator, null);
        ResolutionService resolutionService = EasyMock.createMock(ResolutionService.class);
        PromotionNormalizer normalizer = EasyMock.createMock(PromotionNormalizer.class);

        logicalModelInstantiator =
                new LogicalModelInstantiatorImpl(resolutionService, normalizer, null, atomicComponentInstantiator, compositeComponentInstantiator);
        parent = new LogicalCompositeComponent(PARENT_URI, null, null, null);
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
