/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.net.generator;

import java.net.URI;
import javax.xml.namespace.QName;

import org.osoa.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.net.model.HttpBindingDefinition;
import org.fabric3.spi.binding.provider.BindingMatchResult;
import org.fabric3.spi.binding.provider.BindingProvider;
import org.fabric3.spi.binding.provider.BindingSelectionException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.topology.DomainManager;

/**
 * Creates logical configuration for binding.sca using the HTTP binding.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class HttpBindingProvider implements BindingProvider {
    public static final QName HTTP_BINDING = new QName(Constants.SCA_NS, "binding.http");

    private DomainManager domainManager;

    /**
     * Injects the domain manager. This reference is optional so the extension can be loaded on non-controller instances.
     *
     * @param domainManager the domain manager
     */
    @Reference(required = false)
    public void setDomainManager(DomainManager domainManager) {
        this.domainManager = domainManager;
    }

    public QName getType() {
        return HTTP_BINDING;
    }

    public BindingMatchResult canBind(LogicalReference source, LogicalService target) {
        return new BindingMatchResult(true, HTTP_BINDING);
    }

    public void bind(LogicalReference source, LogicalService target) throws BindingSelectionException {
        if (domainManager == null) {
            throw new BindingSelectionException("Domain manager not configured");
        }
        LogicalComponent<?> targetComponent = target.getParent();
        String targetZone = targetComponent.getZone();
        if (targetZone == null) {
            // programming error
            throw new AssertionError("Target component not allocated: " + targetComponent.getUri());
        }

        // get the base URL for the target cluster
        String targetBaseUrl = domainManager.getTransportMetaData(targetZone, String.class, "binding.net.http");
        if (targetBaseUrl == null) {
            throw new BindingSelectionException("Target HTTP information not found");
        }
        targetBaseUrl = "http://" + targetBaseUrl;
        // determing whether to configure both sides of the wire or just the reference
        if (target.getBindings().isEmpty()) {
            // configure both sides
            configureService(source, target);
            configureReference(source, target, targetBaseUrl);
        } else {
            configureReference(source, target, targetBaseUrl);
        }
        if (target.getDefinition().getServiceContract().getCallbackContract() != null) {
            configureCallback(source, target);

        }
    }


    @SuppressWarnings("unchecked")
    private void configureReference(LogicalReference source, LogicalService target, String baseUrl) throws BindingSelectionException {
        LogicalBinding<HttpBindingDefinition> binding = null;
        for (LogicalBinding<?> entry : target.getBindings()) {
            if (entry.getDefinition().getType().equals(HTTP_BINDING)) {
                binding = (LogicalBinding<HttpBindingDefinition>) entry;
                break;
            }
        }
        if (binding == null) {
            throw new BindingSelectionException("HTTP binding on service not found: " + target.getUri());
        }
        URI targetUri = URI.create(baseUrl + binding.getDefinition().getTargetUri().toString());
        constructLogicalReference(source, targetUri);
    }

    private void constructLogicalReference(LogicalReference source, URI targetUri) {
        HttpBindingDefinition referenceDefinition = new HttpBindingDefinition(targetUri, null);
        LogicalBinding<HttpBindingDefinition> referenceBinding = new LogicalBinding<HttpBindingDefinition>(referenceDefinition, source);
        referenceBinding.setAssigned(true);
        source.addBinding(referenceBinding);
    }

    private void configureService(LogicalReference source, LogicalService target) {
        String endpointName = target.getUri().getPath() + "/" + target.getUri().getFragment();
        URI endpointUri = URI.create(endpointName);
        HttpBindingDefinition serviceDefinition = new HttpBindingDefinition(endpointUri, null);
        QName deployable = source.getParent().getDeployable();
        LogicalBinding<HttpBindingDefinition> serviceBinding = new LogicalBinding<HttpBindingDefinition>(serviceDefinition, target, deployable);
        serviceBinding.setAssigned(true);
        target.addBinding(serviceBinding);
    }

    private void configureCallback(LogicalReference source, LogicalService target) throws BindingSelectionException {
        LogicalComponent<?> sourceComponent = source.getParent();
        String sourceZone = sourceComponent.getZone();
        if (sourceZone == null) {
            // programming error
            LogicalComponent<?> targetComponent = target.getParent();
            throw new AssertionError("Source component not allocated: " + targetComponent.getUri());
        }

        // get the base URL for the source cluster
        String sourceBaseUrl = domainManager.getTransportMetaData(sourceZone, String.class, "binding.net.http");
        if (sourceBaseUrl == null) {
            throw new BindingSelectionException("Source HTTP information not found");
        }
        sourceBaseUrl = "http://" + sourceBaseUrl;
        // configure the callback service on the source side
        String endpointName = target.getUri().getPath() + "/" + source.getUri().getFragment();
        URI endpointUri = URI.create(endpointName);

        HttpBindingDefinition sourceCallbackDefinition = new HttpBindingDefinition(endpointUri, null);
        LogicalBinding<HttpBindingDefinition> sourceBinding = new LogicalBinding<HttpBindingDefinition>(sourceCallbackDefinition, source);
        sourceBinding.setAssigned(true);
        source.addCallbackBinding(sourceBinding);

        URI callbackUri = URI.create(sourceBaseUrl + endpointName);
        HttpBindingDefinition targetCallbackDefinition = new HttpBindingDefinition(callbackUri, null);
        LogicalBinding<HttpBindingDefinition> targetBinding = new LogicalBinding<HttpBindingDefinition>(targetCallbackDefinition, target);
        targetBinding.setAssigned(true);
        target.addCallbackBinding(targetBinding);
    }

}
