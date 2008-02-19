package org.fabric3.fabric.wire;

import java.net.URI;

import junit.framework.TestCase;

import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.wire.PromotionException;
import org.fabric3.spi.wire.PromotionService;

public class DefaultPromotionServiceTestCase extends TestCase {
    
    private PromotionService promotionService = new DefaultPromotionService();
    private LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("fabric3://./runtime"), URI.create("runtime"), null, null);

    public void testNoComponentForPromotedService() {
        
        LogicalService logicalService = new LogicalService(URI.create("service"), null, domain);
        logicalService.setPromotedUri(URI.create("component#service"));
        
        try {
            promotionService.promote(logicalService);
        } catch(PromotionException ex) {
            return;
        }
        
        fail("Expected exception");
        
    }

    public void testNoComponentForPromotedReference() {
        
        LogicalReference logicalReference = new LogicalReference(URI.create("reference"), null, domain);
        logicalReference.addPromotedUri(URI.create("component#service"));
        
        try {
            promotionService.promote(logicalReference);
        } catch(PromotionException ex) {
            return;
        }
        
        fail("Expected exception");
        
    }

}
