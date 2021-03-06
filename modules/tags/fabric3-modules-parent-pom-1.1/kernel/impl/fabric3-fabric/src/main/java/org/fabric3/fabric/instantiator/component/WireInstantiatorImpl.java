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
package org.fabric3.fabric.instantiator.component;

import java.net.URI;

import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.WireInstantiator;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.WireDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.util.UriHelper;

/**
 * Default implementation of the WireInstantiator.
 *
 * @version $Revision$ $Date$
 */
public class WireInstantiatorImpl implements WireInstantiator {

    public void instantiateWires(Composite composite, LogicalCompositeComponent parent, InstantiationContext context) {
        String baseUri = parent.getUri().toString();
        // instantiate wires held directly in the composite and in included composites
        for (WireDefinition definition : composite.getWires()) {
            // resolve the source reference
            // source URI is relative to the parent composite the include is targeted to
            URI sourceUri = URI.create(baseUri + "/" + UriHelper.getDefragmentedName(definition.getSource()));
            String referenceName = definition.getSource().getFragment();
            LogicalReference logicalReference = resolveLogicalReference(referenceName, sourceUri, parent, context);
            if (logicalReference == null) {
                // error resolving, continue
                continue;
            }

            // resolve the target service
            URI targetUri = URI.create(baseUri + "/" + definition.getTarget());
            targetUri = resolveTargetUri(targetUri, parent, context);
            if (targetUri == null) {
                // error resolving
                continue;
            }

            // create the wire
            LogicalWire wire = new LogicalWire(parent, logicalReference, targetUri);
            parent.addWire(logicalReference, wire);
        }
    }

    private LogicalReference resolveLogicalReference(String referenceName,
                                                     URI sourceUri,
                                                     LogicalCompositeComponent parent,
                                                     InstantiationContext context) {
        LogicalComponent<?> source = parent.getComponent(sourceUri);
        if (source == null) {
            URI uri = parent.getUri();
            URI contributionUri = parent.getDefinition().getContributionUri();
            WireSourceNotFound error = new WireSourceNotFound(sourceUri, uri, contributionUri);
            context.addError(error);
            return null;
        }
        LogicalReference logicalReference;
        if (referenceName == null) {
            // a reference was not specified
            if (source.getReferences().size() == 0) {
                URI uri = parent.getUri();
                URI contributionUri = parent.getDefinition().getContributionUri();
                WireSourceNoReference error = new WireSourceNoReference(sourceUri, uri, contributionUri);
                context.addError(error);
                return null;
            } else if (source.getReferences().size() != 1) {
                URI uri = parent.getUri();
                URI contributionUri = parent.getDefinition().getContributionUri();
                WireSourceAmbiguousReference error = new WireSourceAmbiguousReference(sourceUri, uri, contributionUri);
                context.addError(error);
                return null;
            }
            // default to the only reference
            logicalReference = source.getReferences().iterator().next();
        } else {
            logicalReference = source.getReference(referenceName);
            if (logicalReference == null) {
                URI uri = parent.getUri();
                URI contributionUri = parent.getDefinition().getContributionUri();
                WireSourceReferenceNotFound error = new WireSourceReferenceNotFound(sourceUri, referenceName, uri, contributionUri);
                context.addError(error);
                return null;
            }
        }
        return logicalReference;
    }

    /**
     * Resolves the wire target URI to a service provided by a component in the parent composite.
     *
     * @param targetUri the atrget URI to resolve.
     * @param parent    the parent composite to resolve against
     * @param context   the logical context to report errors against
     * @return the fully resolved wire target URI
     */
    private URI resolveTargetUri(URI targetUri, LogicalCompositeComponent parent, InstantiationContext context) {
        URI targetComponentUri = UriHelper.getDefragmentedName(targetUri);
        LogicalComponent<?> targetComponent = parent.getComponent(targetComponentUri);
        if (targetComponent == null) {
            URI uri = parent.getUri();
            URI contributionUri = parent.getDefinition().getContributionUri();
            WireTargetNotFound error = new WireTargetNotFound(targetUri, uri, contributionUri);
            context.addError(error);
            return null;
        }

        String serviceName = targetUri.getFragment();
        if (serviceName != null) {
            if (targetComponent.getService(serviceName) == null) {
                URI uri = parent.getUri();
                URI contributionUri = parent.getDefinition().getContributionUri();
                WireTargetServiceNotFound error = new WireTargetServiceNotFound(targetUri, uri, contributionUri);
                context.addError(error);
                return null;
            }
            return targetUri;
        } else {
            LogicalService target = null;
            for (LogicalService service : targetComponent.getServices()) {
                if (service.getDefinition().isManagement()) {
                    continue;
                }
                if (target != null) {
                    URI uri = parent.getUri();
                    URI contributionUri = parent.getDefinition().getContributionUri();
                    AmbiguousWireTargetService error = new AmbiguousWireTargetService(uri, targetUri, contributionUri);
                    context.addError(error);
                    return null;
                }
                target = service;
            }
            if (target == null) {
                URI uri = parent.getUri();
                URI contributionUri = parent.getDefinition().getContributionUri();
                WireTargetNoService error = new WireTargetNoService(targetUri, uri, contributionUri);
                context.addError(error);
                return null;
            }
            return target.getUri();
        }

    }

}
