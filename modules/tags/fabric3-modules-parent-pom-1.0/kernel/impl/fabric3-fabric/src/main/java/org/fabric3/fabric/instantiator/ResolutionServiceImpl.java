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

import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Default implementation of the resolution service. Resolves promotions first and subsequently invokes a series of resolvers to determine reference
 * targets.
 *
 * @version $Revision$ $Date$
 */
public class ResolutionServiceImpl implements ResolutionService {

    private final PromotionResolutionService promotionResolutionService;
    private final List<TargetResolutionService> targetResolutionServices;

    /**
     * Constructor.
     *
     * @param promotionResolutionService Service for handling promotions.
     * @param targetResolutionServices   An ordered list of target resolution services.
     */
    public ResolutionServiceImpl(@Reference PromotionResolutionService promotionResolutionService,
                                 @Reference List<TargetResolutionService> targetResolutionServices) {
        this.promotionResolutionService = promotionResolutionService;
        this.targetResolutionServices = targetResolutionServices;
    }

    public void resolve(LogicalComponent<?> logicalComponent, InstantiationContext context) {
        if (logicalComponent instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent compositeComponent = (LogicalCompositeComponent) logicalComponent;
            for (LogicalComponent<?> child : compositeComponent.getComponents()) {
                resolve(child, context);
            }
        }

        resolveReferences(logicalComponent, context);
        resolveServices(logicalComponent, context);
    }

    public void resolve(LogicalService logicalService, InstantiationContext context) {
        promotionResolutionService.resolve(logicalService, context);
    }

    public void resolve(LogicalReference reference, LogicalCompositeComponent component, InstantiationContext context) {
        promotionResolutionService.resolve(reference, context);
        for (TargetResolutionService targetResolutionService : targetResolutionServices) {
            targetResolutionService.resolve(reference, component, context);
        }
    }

    /*
     * Handles promotions and target resolution on references.
     */
    private void resolveReferences(LogicalComponent<?> logicalComponent, InstantiationContext context) {
        LogicalCompositeComponent parent = logicalComponent.getParent();
        for (LogicalReference logicalReference : logicalComponent.getReferences()) {
            Multiplicity multiplicityValue = logicalReference.getDefinition().getMultiplicity();
            boolean refMultiplicity = multiplicityValue.equals(Multiplicity.ZERO_N) || multiplicityValue.equals(Multiplicity.ONE_N);
            if (refMultiplicity || !logicalReference.isResolved()) {
                // Only resolve references that have not been resolved or ones that are multiplicities since the latter may be reinjected.
                // Explicitly set the reference to unresolved, since if it was a multiplicity it may have been previously resolved.
                logicalReference.setResolved(false);
                promotionResolutionService.resolve(logicalReference, context);
                for (TargetResolutionService targetResolutionService : targetResolutionServices) {
                    targetResolutionService.resolve(logicalReference, parent, context);
                    if (logicalReference.isResolved()) {
                        // the reference has been resolved
                        break;
                    }
                }
            }
        }
    }

    /*
     * Handles promotions on services.
     */
    private void resolveServices(LogicalComponent<?> logicalComponent, InstantiationContext context) {
        for (LogicalService logicalService : logicalComponent.getServices()) {
            promotionResolutionService.resolve(logicalService, context);
        }
    }

}
