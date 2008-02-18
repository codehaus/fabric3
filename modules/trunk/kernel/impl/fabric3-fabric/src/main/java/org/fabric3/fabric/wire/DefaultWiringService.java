package org.fabric3.fabric.wire;

import java.util.List;

import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.wire.PromotionService;
import org.fabric3.spi.wire.TargetResolutionService;
import org.fabric3.spi.wire.WiringService;
import org.osoa.sca.annotations.Reference;

/**
 * Default implementation of the wiring service.
 * 
 * @version $Revision$ $Date$
 *
 */
public class DefaultWiringService implements WiringService {
    
    private final PromotionService promotionService;
    private final List<TargetResolutionService> targetResolutionServices;

    /**
     * Injects the references required for wiring components.
     * 
     * @param promotionService Service for handling promotions.
     * @param targetResolutionServices An ordered list of target resolution services.
     */
    public DefaultWiringService(@Reference PromotionService promotionService, 
                                @Reference List<TargetResolutionService> targetResolutionServices) {
        this.promotionService = promotionService;
        this.targetResolutionServices = targetResolutionServices;
    }

    /**
     * Recursively wire components. If this is a composite component, all the child components 
     * are wired. For atomic components, promotions on services and references are handled first. 
     * Then for promoted references, targets are resolved in the context of the containing 
     * composite using an ordered list of target resolution services. A percieved order for 
     * resolution is explicit target, intent based autowire and type based autowire.
     * 
     * @param logicalComponent Logical component that needs to be wired.
     * @throws WiringException Ifthe target for a required reference is unable to be wired.
     * @see org.fabric3.spi.wire.WiringService#wire(org.fabric3.spi.model.instance.LogicalComponent)
     */
    public void wire(LogicalComponent<?> logicalComponent) throws WiringException {
        
        if (logicalComponent instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent compositeComponent = (LogicalCompositeComponent) logicalComponent;
            for (LogicalComponent<?> child : compositeComponent.getComponents()) {
                wire(child);
            }
        } else {
            handleServices(logicalComponent);
            handleReferences(logicalComponent);
        }
        
    }

    /*
     * Handles promotions and target resolution on references.
     */
    private void handleReferences(LogicalComponent<?> logicalComponent) throws WiringException {
        
        for (LogicalReference logicalReference : logicalComponent.getReferences()) {
            promotionService.promote(logicalReference);
            boolean resolved = false;
            for (TargetResolutionService targetResolutionService : targetResolutionServices) {
                if (targetResolutionService.resolve(logicalReference, logicalComponent.getParent())) {
                    break;
                }
            }
            if (!resolved && logicalReference.getDefinition().isRequired()) {
                throw new WiringException("Unable to resolve reference " + logicalReference.getUri());
            }
        }
        
    }

    /*
     * Handles promotions on services.
     */
    private void handleServices(LogicalComponent<?> logicalComponent) {
        
        for (LogicalService logicalService : logicalComponent.getServices()) {
            promotionService.promote(logicalService);
        }
        
    }

}
