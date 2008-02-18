package org.fabric3.fabric.wire;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.PromotionException;
import org.fabric3.spi.wire.PromotionService;

/**
 * Default implementation of the promotion service.
 * 
 * @version $Revision$ $Date$
 *
 */
public class DefaultPromotionService implements PromotionService {
    
    /**
     * Handles the promotion on the specified logical service. 
     * 
     * Promoted URIs are os the general form <code>componentId#serviceName</code>, 
     * where the service name is optional. If the  promoted URI doesn't contain a 
     * fragment for the service name, the promoted component is expected to have 
     * exactly one service. If the service fragment is present the promoted 
     * component is required to have a service by the name. If the service fragment 
     * was not specified, the promoted URI is set to the URI of the promoted 
     * service.
     * 
     * @param logicalService Logical service whose promotion is handled.
     */
    public void promote(LogicalService logicalService) throws PromotionException {

        URI promotedUri = logicalService.getPromote();
        
        if (promotedUri == null) {
            return;
        }

        URI promotedComponentUri = UriHelper.getDefragmentedName(promotedUri);
        String promotedServiceName = promotedUri.getFragment();
        
        LogicalCompositeComponent composite = (LogicalCompositeComponent) logicalService.getParent();
        LogicalComponent<?> promotedComponent = composite.getComponent(promotedComponentUri);
        
        if (promotedComponent == null) {
            throw new PromotionException("Promoted component not found:" + promotedUri);
        }

        if (promotedServiceName == null) {
            Collection<LogicalService> componentServices = promotedComponent.getServices();
            if (componentServices.size() == 0) {
                throw new PromotionException("Promoted service not found:" + promotedUri);
            } else if (componentServices.size() != 1) {
                throw new PromotionException("More than one service available on promoted component:" + promotedUri);
            }
            logicalService.setPromote(componentServices.iterator().next().getUri());
        } else {
            if (promotedComponent.getService(promotedUri.getFragment()) == null) {
                throw new PromotionException("Promoted service not found:" + promotedUri);
            }
        }
        
    }
    
    /**
     * Handles all the promotions on the specified logical reference. 
     * 
     * Promoted URIs are os the general form <code>componentId#referenceName</code>, 
     * where the reference name is optional. If the  promoted URI doesn't contain a 
     * fragment for the reference name, the promoted component is expected to have 
     * exactly one reference. If the reference fragment is present the promoted 
     * component is required to have a reference by the name. If the reference fragment 
     * was not specified, the promoted URI is set to the URI of the promoted 
     * reference.
     * 
     * @param logicalReference Logical reference whose promotion is handled.
     */
    public void promote(LogicalReference logicalReference) throws PromotionException {
        
        List<URI> promotedUris = logicalReference.getPromotedUris();
        
        for (int i = 0; i < promotedUris.size(); i++) {
            
            URI promotedUri = promotedUris.get(i);
            
            URI promotedComponentUri = UriHelper.getDefragmentedName(promotedUri);
            String promotedReferenceName = promotedUri.getFragment();
            
            LogicalCompositeComponent parent = (LogicalCompositeComponent) logicalReference.getParent();
            LogicalComponent<?> promotedComponent = parent.getComponent(promotedComponentUri);
            
            if (promotedComponent == null) {
                throw new PromotionException("Promoted component not found:" + promotedUri);
            }
            
            if (promotedReferenceName == null) {
                Collection<LogicalReference> componentReferences = promotedComponent.getReferences();
                if (componentReferences.size() == 0) {
                    throw new PromotionException("Promoted reference not found:" + promotedUri);
                } else if (componentReferences.size() > 1) {
                    throw new PromotionException("More than one reference available on promoted component:" + promotedUri);
                }
                logicalReference.setPromotedUri(i, componentReferences.iterator().next().getUri());
            } else if (promotedComponent.getReference(promotedReferenceName) == null) {
                throw new PromotionException("Promoted reference not found:" + promotedUri);
            }

        }
        
    }

}
