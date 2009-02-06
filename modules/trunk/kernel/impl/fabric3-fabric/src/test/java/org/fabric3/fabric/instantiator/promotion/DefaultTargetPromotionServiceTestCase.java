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
 */
package org.fabric3.fabric.instantiator.promotion;

import java.net.URI;

import junit.framework.TestCase;

import org.fabric3.fabric.instantiator.AmbiguousReference;
import org.fabric3.fabric.instantiator.AmbiguousService;
import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.NoServiceOnComponent;
import org.fabric3.fabric.instantiator.PromotedComponentNotFound;
import org.fabric3.fabric.instantiator.PromotionResolutionService;
import org.fabric3.fabric.instantiator.ReferenceNotFound;
import org.fabric3.fabric.instantiator.ServiceNotFound;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.system.scdl.SystemImplementation;

public class DefaultTargetPromotionServiceTestCase extends TestCase {

    private PromotionResolutionService promotionResolutionService;
    private LogicalCompositeComponent domain;

    public void testNoComponentForPromotedService() {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component#service"));

        InstantiationContext change = new InstantiationContext(domain);
        promotionResolutionService.resolve(logicalService, change);
        assertTrue(change.getErrors().get(0) instanceof PromotedComponentNotFound);
    }

    public void testMultipleServicesWithNoServiceFragment() throws Exception {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addService(new LogicalService(URI.create("component#service1"), null, domain));
        logicalComponent.addService(new LogicalService(URI.create("component#service2"), null, domain));

        domain.addComponent(logicalComponent);
        InstantiationContext change = new InstantiationContext(domain);
        promotionResolutionService.resolve(logicalService, change);
        assertTrue(change.getErrors().get(0) instanceof AmbiguousService);
    }

    public void testNoServiceWithNoServiceFragment() throws Exception {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);

        domain.addComponent(logicalComponent);
        InstantiationContext change = new InstantiationContext(domain);
        promotionResolutionService.resolve(logicalService, change);
        assertTrue(change.getErrors().get(0) instanceof NoServiceOnComponent);
    }

    public void testNoServiceWithServiceFragment() throws Exception {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component#service"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);

        domain.addComponent(logicalComponent);

        InstantiationContext change = new InstantiationContext(domain);
        promotionResolutionService.resolve(logicalService, change);
        assertTrue(change.getErrors().get(0) instanceof ServiceNotFound);
    }

    public void testNoServiceFragment() {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addService(new LogicalService(URI.create("component#service1"), null, domain));
        domain.addComponent(logicalComponent);

        InstantiationContext change = new InstantiationContext(domain);
        promotionResolutionService.resolve(logicalService, change);
        assertEquals(URI.create("component#service1"), logicalService.getPromotedUri());

    }

    public void testWithServiceFragment() {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component#service1"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addService(new LogicalService(URI.create("component#service1"), null, domain));
        domain.addComponent(logicalComponent);
        InstantiationContext change = new InstantiationContext(domain);
        promotionResolutionService.resolve(logicalService, change);
    }

    public void testNoComponentForPromotedReference() {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component#service"));

        InstantiationContext change = new InstantiationContext(domain);
        promotionResolutionService.resolve(logicalReference, change);
        assertTrue(change.getErrors().get(0) instanceof PromotedComponentNotFound);

    }

    public void testMultipleReferencesWithNoReferenceFragment() throws Exception {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addReference(new LogicalReference(URI.create("component#reference1"), null, domain));
        logicalComponent.addReference(new LogicalReference(URI.create("component#reference2"), null, domain));

        domain.addComponent(logicalComponent);

        InstantiationContext context = new InstantiationContext(domain);
        promotionResolutionService.resolve(logicalReference, context);
        assertTrue(context.getErrors().get(0) instanceof AmbiguousReference);

    }

    public void testNoReferenceWithNoReferenceFragment() {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);


        domain.addComponent(logicalComponent);

        InstantiationContext context = new InstantiationContext(domain);
        promotionResolutionService.resolve(logicalReference, context);
        assert (context.getErrors().get(0) instanceof ReferenceNotFound);
    }

    public void testNoReferenceWithReferenceFragment() {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component#reference"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);


        domain.addComponent(logicalComponent);

        InstantiationContext context = new InstantiationContext(domain);
        promotionResolutionService.resolve(logicalReference, context);
        assertTrue(context.getErrors().get(0) instanceof ReferenceNotFound);
    }

    public void testNoReferenceFragment() {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addReference(new LogicalReference(URI.create("component#reference1"), null, domain));
        domain.addComponent(logicalComponent);

        InstantiationContext context = new InstantiationContext(domain);
        promotionResolutionService.resolve(logicalReference, context);
        assertEquals(URI.create("component#reference1"), logicalReference.getPromotedUris().iterator().next());

    }

    public void testWithReferenceFragment() {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component#reference1"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addReference(new LogicalReference(URI.create("component#reference1"), null, domain));
        domain.addComponent(logicalComponent);

        InstantiationContext context = new InstantiationContext(domain);
        promotionResolutionService.resolve(logicalReference, context);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        promotionResolutionService = new DefaultPromotionResolutionService();
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>("domain");
        URI uri = URI.create("fabric3://runtime");
        domain = new LogicalCompositeComponent(uri, definition, null);
    }
}
