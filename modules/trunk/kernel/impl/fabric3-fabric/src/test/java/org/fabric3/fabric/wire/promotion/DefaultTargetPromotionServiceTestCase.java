package org.fabric3.fabric.wire.promotion;

import java.net.URI;

import junit.framework.TestCase;

import org.fabric3.system.scdl.SystemImplementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.wire.TargetPromotionService;

public class DefaultTargetPromotionServiceTestCase extends TestCase {
    
    private TargetPromotionService promotionService = new DefaultTargetPromotionService();
    private LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("fabric3://./runtime"), URI.create("runtime"), null, null);

    public void testNoComponentForPromotedService() {
        
        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component#service"));
        
        try {
            promotionService.promote(logicalService);
        } catch(PromotedComponentNotFoundException ex) {
            return;
        }
        
        fail("Expected exception");
        
    }

    public void testMultipleServicesWithNoServiceFragment() {
        
        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component"));
        
        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"), 
                                                                                                             URI.create("runtime"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addService(new LogicalService(URI.create("component#service1"), null, domain));
        logicalComponent.addService(new LogicalService(URI.create("component#service2"), null, domain));
        
        domain.addComponent(logicalComponent);
        
        try {
            promotionService.promote(logicalService);
        } catch(AmbiguousServiceException ex) {
            return;
        }
        
        fail("Expected exception");
        
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
            promotionService.promote(logicalService);
        } catch(NoServiceOnComponentException ex) {
            return;
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
            promotionService.promote(logicalService);
        } catch(ServiceNotFoundException ex) {
            return;
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
        
        promotionService.promote(logicalService);
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
        
        promotionService.promote(logicalService);
        
    }

    public void testNoComponentForPromotedReference() {
        
        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component#service"));
        
        try {
            promotionService.promote(logicalReference);
        } catch(PromotedComponentNotFoundException ex) {
            return;
        }
        
        fail("Expected exception");
        
    }

    public void testMultipleReferencesWithNoReferenceFragment() {
        
        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component"));
        
        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"), 
                                                                                                             URI.create("runtime"),
                                                                                                             null,
                                                                                                             domain);
        logicalComponent.addReference(new LogicalReference(URI.create("component#reference1"), null, domain));
        logicalComponent.addReference(new LogicalReference(URI.create("component#reference2"), null, domain));
        
        domain.addComponent(logicalComponent);
        
        try {
            promotionService.promote(logicalReference);
        } catch(AmbiguousReferenceException ex) {
            return;
        }
        
        fail("Expected exception");
        
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
            promotionService.promote(logicalReference);
        } catch(NoReferenceOnComponentException ex) {
            return;
        }
        
        fail("Expected exception");
        
    }

    public void testNoReferenceWithReferenceFragment() {
        
        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component#reference"));
        
        LogicalComponent<SystemImplementation> logicalComponent = new LogicalComponent<SystemImplementation>(URI.create("component"), 
                                                                                                             URI.create("runtime"),
                                                                                                             null,
                                                                                                             domain);

        
        domain.addComponent(logicalComponent);
        
        try {
            promotionService.promote(logicalReference);
        } catch(ReferenceNotFoundException ex) {
            return;
        }
        
        fail("Expected exception");
        
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
        
        promotionService.promote(logicalReference);
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
        
        promotionService.promote(logicalReference);
        
    }

}
