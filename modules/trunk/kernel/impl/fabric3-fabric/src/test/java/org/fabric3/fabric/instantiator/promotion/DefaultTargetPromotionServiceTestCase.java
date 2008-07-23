package org.fabric3.fabric.instantiator.promotion;

import java.net.URI;

import junit.framework.TestCase;

import org.fabric3.fabric.instantiator.AmbiguousReference;
import org.fabric3.fabric.instantiator.AmbiguousService;
import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.fabric.instantiator.LogicalInstantiationException;
import org.fabric3.fabric.instantiator.NoServiceOnComponentException;
import org.fabric3.fabric.instantiator.PromotedComponentNotFoundException;
import org.fabric3.fabric.instantiator.ReferenceNotFound;
import org.fabric3.fabric.instantiator.ServiceNotFoundException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.system.scdl.SystemImplementation;

public class DefaultTargetPromotionServiceTestCase extends TestCase {

    private PromotionResolutionService promotionResolutionService = new DefaultPromotionResolutionService();
    private LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("fabric3://./runtime"), URI.create("runtime"), null, null);

    public void testNoComponentForPromotedService() {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component#service"));

        try {
            LogicalChange change = new LogicalChange(domain);
            promotionResolutionService.resolve(logicalService, change);
        } catch (PromotedComponentNotFoundException ex) {
            return;
        } catch (LogicalInstantiationException e) {
            fail("Unexpected exception");
        }

        fail("Expected exception");

    }

    public void testMultipleServicesWithNoServiceFragment() throws Exception{

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             URI.create("runtime"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addService(new LogicalService(URI.create("component#service1"), null, domain));
        logicalComponent.addService(new LogicalService(URI.create("component#service2"), null, domain));

        domain.addComponent(logicalComponent);
        LogicalChange change = new LogicalChange(domain);
        promotionResolutionService.resolve(logicalService, change);
        assertTrue(change.getErrors().get(0) instanceof AmbiguousService);
    }

    public void testNoServiceWithNoServiceFragment() {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             URI.create("runtime"),
                                                                                                             null,
                                                                                                             domain);

        domain.addComponent(logicalComponent);

        try {
            LogicalChange change = new LogicalChange(domain);
            promotionResolutionService.resolve(logicalService, change);
        } catch (NoServiceOnComponentException ex) {
            return;
        } catch (LogicalInstantiationException e) {
            fail("Unexpected exception");
        }

        fail("Expected exception");

    }

    public void testNoServiceWithServiceFragment() {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component#service"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             URI.create("runtime"),
                                                                                                             null,
                                                                                                             domain);

        domain.addComponent(logicalComponent);

        try {
            LogicalChange change = new LogicalChange(domain);
            promotionResolutionService.resolve(logicalService, change);
        } catch (ServiceNotFoundException ex) {
            return;
        } catch (LogicalInstantiationException e) {
            fail("Unexpected exception");
        }

        fail("Expected exception");

    }

    public void testNoServiceFragment() {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             URI.create("runtime"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addService(new LogicalService(URI.create("component#service1"), null, domain));
        domain.addComponent(logicalComponent);

        try {
            LogicalChange change = new LogicalChange(domain);
            promotionResolutionService.resolve(logicalService, change);
        } catch (LogicalInstantiationException e) {
            fail("Unexpected exception");
        }
        assertEquals(URI.create("component#service1"), logicalService.getPromotedUri());

    }

    public void testWithServiceFragment() {

        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component#service1"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             URI.create("runtime"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addService(new LogicalService(URI.create("component#service1"), null, domain));
        domain.addComponent(logicalComponent);

        try {
            LogicalChange change = new LogicalChange(domain);
            promotionResolutionService.resolve(logicalService, change);
        } catch (LogicalInstantiationException e) {
            fail("Unexpected exception");
        }

    }

    public void testNoComponentForPromotedReference() {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component#service"));

        try {
            LogicalChange change = new LogicalChange(domain);
            promotionResolutionService.resolve(logicalReference, change);
        } catch (PromotedComponentNotFoundException ex) {
            return;
        } catch (LogicalInstantiationException e) {
            fail("Unexpected exception");
        }

        fail("Expected exception");

    }

    public void testMultipleReferencesWithNoReferenceFragment() throws Exception {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             URI.create("runtime"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addReference(new LogicalReference(URI.create("component#reference1"), null, domain));
        logicalComponent.addReference(new LogicalReference(URI.create("component#reference2"), null, domain));

        domain.addComponent(logicalComponent);

        LogicalChange change = new LogicalChange(domain);
        promotionResolutionService.resolve(logicalReference, change);
        assertTrue(change.getErrors().get(0) instanceof AmbiguousReference);

    }

    public void testNoReferenceWithNoReferenceFragment() {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             URI.create("runtime"),
                                                                                                             null,
                                                                                                             domain);


        domain.addComponent(logicalComponent);

        try {
            LogicalChange change = new LogicalChange(domain);
            promotionResolutionService.resolve(logicalReference, change);
            assert (change.getErrors().get(0) instanceof ReferenceNotFound);
        } catch (LogicalInstantiationException e) {
            fail("Unexpected exception");
        }
    }

    public void testNoReferenceWithReferenceFragment() throws LogicalInstantiationException {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component#reference"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             URI.create("runtime"),
                                                                                                             null,
                                                                                                             domain);


        domain.addComponent(logicalComponent);

        LogicalChange change = new LogicalChange(domain);
        promotionResolutionService.resolve(logicalReference, change);
        assertTrue(change.getErrors().get(0) instanceof ReferenceNotFound);
    }

    public void testNoReferenceFragment() {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             URI.create("runtime"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addReference(new LogicalReference(URI.create("component#reference1"), null, domain));
        domain.addComponent(logicalComponent);

        try {
            LogicalChange change = new LogicalChange(domain);
            promotionResolutionService.resolve(logicalReference, change);
        } catch (LogicalInstantiationException e) {
            fail("Unexpected exception");
        }
        assertEquals(URI.create("component#reference1"), logicalReference.getPromotedUris().iterator().next());

    }

    public void testWithReferenceFragment() {

        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component#reference1"));

        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"),
                                                                                                             URI.create("runtime"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addReference(new LogicalReference(URI.create("component#reference1"), null, domain));
        domain.addComponent(logicalComponent);

        try {
            LogicalChange change = new LogicalChange(domain);
            promotionResolutionService.resolve(logicalReference, change);
        } catch (LogicalInstantiationException e) {
            fail("Unexpected exception");
        }

    }

}
