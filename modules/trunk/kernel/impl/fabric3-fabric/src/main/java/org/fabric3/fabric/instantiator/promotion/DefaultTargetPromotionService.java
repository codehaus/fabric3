package org.fabric3.fabric.instantiator.promotion;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.fabric.instantiator.PromotionException;
import org.fabric3.fabric.instantiator.promotion.TargetPromotionService;

/**
 * Default implementation of the promotion service.
 * 
 * @version $Revision$ $Date$
 *
 */
public class DefaultTargetPromotionService implements TargetPromotionService {
    
    public void promote(LogicalService logicalService) throws PromotionException {

        URI promotedUri = logicalService.getPromotedUri();
        
        if (promotedUri == null) {
            return;
        }

        URI promotedComponentUri = UriHelper.getDefragmentedName(promotedUri);
        String promotedServiceName = promotedUri.getFragment();
        
        LogicalCompositeComponent composite = (LogicalCompositeComponent) logicalService.getParent();
        LogicalComponent<?> promotedComponent = composite.getComponent(promotedComponentUri);
        
        if (promotedComponent == null) {
            throw new PromotedComponentNotFoundException(promotedComponentUri);
        }

        if (promotedServiceName == null) {
            Collection<LogicalService> componentServices = promotedComponent.getServices();
            if (componentServices.size() == 0) {
                throw new NoServiceOnComponentException(promotedComponentUri);
            } else if (componentServices.size() != 1) {
                throw new AmbiguousServiceException(promotedComponentUri);
            }
            logicalService.setPromotedUri(componentServices.iterator().next().getUri());
        } else {
            if (promotedComponent.getService(promotedServiceName) == null) {
                throw new ServiceNotFoundException(promotedComponentUri, promotedServiceName);
            }
        }
        
    }
    
    public void promote(LogicalReference logicalReference) throws PromotionException {
        
        List<URI> promotedUris = logicalReference.getPromotedUris();
        
        for (int i = 0; i < promotedUris.size(); i++) {
            
            URI promotedUri = promotedUris.get(i);
            
            URI promotedComponentUri = UriHelper.getDefragmentedName(promotedUri);
            String promotedReferenceName = promotedUri.getFragment();
            
            LogicalCompositeComponent parent = (LogicalCompositeComponent) logicalReference.getParent();
            LogicalComponent<?> promotedComponent = parent.getComponent(promotedComponentUri);
            
            if (promotedComponent == null) {
                throw new PromotedComponentNotFoundException(promotedComponentUri);
            }
            
            if (promotedReferenceName == null) {
                Collection<LogicalReference> componentReferences = promotedComponent.getReferences();
                if (componentReferences.size() == 0) {
                    throw new NoReferenceOnComponentException(promotedComponentUri);
                } else if (componentReferences.size() > 1) {
                    throw new AmbiguousReferenceException(promotedComponentUri);
                }
                logicalReference.setPromotedUri(i, componentReferences.iterator().next().getUri());
            } else if (promotedComponent.getReference(promotedReferenceName) == null) {
                throw new ReferenceNotFoundException(promotedComponentUri, promotedReferenceName);
            }

        }
        
    }

}
