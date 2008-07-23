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
package org.fabric3.fabric.instantiator.promotion;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.fabric3.fabric.instantiator.AmbiguousReference;
import org.fabric3.fabric.instantiator.AmbiguousService;
import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.fabric.instantiator.LogicalInstantiationException;
import org.fabric3.fabric.instantiator.NoServiceOnComponent;
import org.fabric3.fabric.instantiator.PromotedComponentNotFoundException;
import org.fabric3.fabric.instantiator.ReferenceNotFound;
import org.fabric3.fabric.instantiator.ServiceNotFound;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.util.UriHelper;

/**
 * Default implementation of the promotion service.
 *
 * @version $Revision$ $Date$
 */
public class DefaultPromotionResolutionService implements PromotionResolutionService {

    public void resolve(LogicalService logicalService, LogicalChange change) throws LogicalInstantiationException {

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
                String msg = "No services available on component: " + promotedComponentUri;
                NoServiceOnComponent error = new NoServiceOnComponent(msg, logicalService);
                change.addError(error);
                return;
            } else if (componentServices.size() != 1) {
                String msg = "The promoted service " + logicalService.getUri() + " must explicitly specify the service it is promoting on component "
                        + promotedComponentUri + " as the component has more than one service";
                AmbiguousService error = new AmbiguousService(logicalService, msg);
                change.addError(error);
                return;
            }
            logicalService.setPromotedUri(componentServices.iterator().next().getUri());
        } else {
            if (promotedComponent.getService(promotedServiceName) == null) {
                String message =
                        "Service " + promotedServiceName + " promoted from " + logicalService.getUri() + "not found on component " + promotedComponentUri;
                ServiceNotFound error = new ServiceNotFound(message, logicalService, promotedComponentUri);
                change.addError(error);
            }
        }

    }

    public void resolve(LogicalReference logicalReference, LogicalChange change) throws LogicalInstantiationException {

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
                    String msg = "Reference " + promotedReferenceName + " not found on component " + promotedComponentUri;
                    ReferenceNotFound error = new ReferenceNotFound(msg, promotedComponent, promotedReferenceName);
                    change.addError(error);
                    return;
                } else if (componentReferences.size() > 1) {
                    AmbiguousReference error = new AmbiguousReference(logicalReference, promotedComponentUri);
                    change.addError(error);
                    return;
                }
                logicalReference.setPromotedUri(i, componentReferences.iterator().next().getUri());
            } else if (promotedComponent.getReference(promotedReferenceName) == null) {
                String msg = "Reference " + promotedReferenceName + " not found on component " + promotedComponentUri;
                ReferenceNotFound error = new ReferenceNotFound(msg, promotedComponent, promotedReferenceName);
                change.addError(error);
                return;
            }

        }

    }

}
