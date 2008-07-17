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

import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.instantiator.promotion.PromotionResolutionService;
import org.fabric3.fabric.instantiator.target.TargetResolutionService;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Default implementation of the wiring service.
 *
 * @version $Revision$ $Date$
 */
public class ResolutionServiceImpl implements ResolutionService {

    private final PromotionResolutionService promotionResolutionService;
    private final List<TargetResolutionService> targetResolutionServices;

    /**
     * Injects the references required for wiring components.
     *
     * @param promotionResolutionService Service for handling promotions.
     * @param targetResolutionServices   An ordered list of target resolution services.
     */
    public ResolutionServiceImpl(@Reference PromotionResolutionService promotionResolutionService,
                                 @Reference List<TargetResolutionService> targetResolutionServices) {
        this.promotionResolutionService = promotionResolutionService;
        this.targetResolutionServices = targetResolutionServices;
    }

    /**
     * Resolve reference targets and promotions for a component. If this is a composite component, all the child components will be resolved. For
     * promoted references, targets are resolved in the context of the containing composite using an ordered list of target resolution services. A
     * percieved order for resolution is explicit target, intent based autowire, and type based autowire.
     *
     * @param logicalComponent Logical component that needs to be wired.
     * @throws LogicalInstantiationException Ifthe target for a required reference is unable to be wired.
     */
    public void resolve(LogicalComponent<?> logicalComponent) throws LogicalInstantiationException {
        if (logicalComponent instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent compositeComponent = (LogicalCompositeComponent) logicalComponent;
            for (LogicalComponent<?> child : compositeComponent.getComponents()) {
                resolve(child);
            }
        }

        resolveReferences(logicalComponent);
        resolveServices(logicalComponent);
    }

    public void resolve(LogicalService logicalService) throws LogicalInstantiationException {
        promotionResolutionService.resolve(logicalService);
    }

    public void resolve(LogicalReference logicalReference, LogicalCompositeComponent context) throws LogicalInstantiationException {

        promotionResolutionService.resolve(logicalReference);
        for (TargetResolutionService targetResolutionService : targetResolutionServices) {
            targetResolutionService.resolve(logicalReference, context);
        }
    }

    /*
     * Handles promotions and target resolution on references.
     */
    private void resolveReferences(LogicalComponent<?> logicalComponent) throws LogicalInstantiationException {
        for (LogicalReference logicalReference : logicalComponent.getReferences()) {
            promotionResolutionService.resolve(logicalReference);
            for (TargetResolutionService targetResolutionService : targetResolutionServices) {
                targetResolutionService.resolve(logicalReference, logicalComponent.getParent());
            }
        }
    }

    /*
     * Handles promotions on services.
     */
    private void resolveServices(LogicalComponent<?> logicalComponent) throws LogicalInstantiationException {
        for (LogicalService logicalService : logicalComponent.getServices()) {
            promotionResolutionService.resolve(logicalService);
        }
    }

}
