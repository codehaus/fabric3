package org.fabric3.fabric.wire;

import java.net.URI;

import junit.framework.TestCase;

import org.fabric3.fabric.implementation.system.SystemImplementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.wire.PromotionException;
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

}
