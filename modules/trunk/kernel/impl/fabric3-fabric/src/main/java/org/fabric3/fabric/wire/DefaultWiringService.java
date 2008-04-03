package org.fabric3.fabric.wire;

import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.wire.PromotionException;
import org.fabric3.spi.wire.TargetPromotionService;
import org.fabric3.spi.wire.TargetResolutionService;
import org.fabric3.spi.wire.WiringService;

/**
 * Default implementation of the wiring service.
 * 
 * @version $Revision$ $Date$
 *
 */
public class DefaultWiringService implements WiringService {
    
    private final TargetPromotionService targetPromotionService;
    private final List<TargetResolutionService> targetResolutionServices;

    /**
     * Injects the references required for wiring components.
     * 
     * @param targetPromotionService Service for handling promotions.
     * @param targetResolutionServices An ordered list of target resolution services.
     */
    public DefaultWiringService(@Reference TargetPromotionService targetPromotionService, 
                                @Reference List<TargetResolutionService> targetResolutionServices) {
        this.targetPromotionService = targetPromotionService;
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
    public void wire(LogicalComponent<?> logicalComponent) throws PromotionException {
        
        if (logicalComponent instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent compositeComponent = (LogicalCompositeComponent) logicalComponent;
            for (LogicalComponent<?> child : compositeComponent.getComponents()) {
                wire(child);
            }
        }

        handleReferences(logicalComponent);
        handleServices(logicalComponent);
        
    }
    
    /**
     * Handles the promotion on the specified logical service. 
     * 
     * @param logicalService Logical service whose promotion is handled.
     */
    public void promote(LogicalService logicalService) throws PromotionException {
        targetPromotionService.promote(logicalService);
    }
    
    /**
     * Resolves the target for a logical reference.
     *
     * @param logicalReference a reference to wire
     * @param context Composite component within which the targets are resolved.
     */
    public void wire(LogicalReference logicalReference, LogicalCompositeComponent context) throws PromotionException {
        
        targetPromotionService.promote(logicalReference);
        for (TargetResolutionService targetResolutionService : targetResolutionServices) {
            targetResolutionService.resolve(logicalReference, context);
        }
    }

    /*
     * Handles promotions and target resolution on references.
     */
    private void handleReferences(LogicalComponent<?> logicalComponent) throws PromotionException {
        for (LogicalReference logicalReference : logicalComponent.getReferences()) {
            targetPromotionService.promote(logicalReference);
            for (TargetResolutionService targetResolutionService : targetResolutionServices) {
                targetResolutionService.resolve(logicalReference, logicalComponent.getParent());
            }
        }
    }

    /*
     * Handles promotions on services.
     */
    private void handleServices(LogicalComponent<?> logicalComponent) throws PromotionException {
        for (LogicalService logicalService : logicalComponent.getServices()) {
            targetPromotionService.promote(logicalService);
        }
    }

}
