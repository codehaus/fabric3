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
package org.fabric3.fabric.binding;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.binding.BindingMatchResult;
import org.fabric3.spi.binding.BindingProvider;
import org.fabric3.spi.binding.BindingSelectionException;
import org.fabric3.spi.binding.BindingSelectionStrategy;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.services.lcm.LogicalComponentManager;
import org.fabric3.spi.util.UriHelper;

/**
 * Selects a binding provider by delegating to a BindingSelectionStrategy configured for the domain. For each wire, if a remote service has an
 * explicit binding, its configuration will be used to construct the reference binding. If a service does not have an explicit binding, the wire is
 * said to using binding.sca, in which case the BindingSelector will select an appropriate remote transport and create binding configuraton for both
 * sides of the wire.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class BindingSelectorImpl implements BindingSelector {
    private List<BindingProvider> providers = new ArrayList<BindingProvider>();
    private BindingSelectionStrategy strategy;
    private LogicalComponentManager logicalComponentManager;

    public BindingSelectorImpl(@Reference(name = "logicalComponentManager") LogicalComponentManager logicalComponentManager) {
        this.logicalComponentManager = logicalComponentManager;
    }

    /**
     * Lazily injects SCAServiceProviders as they become available from runtime extensions.
     *
     * @param providers the set of providers
     */
    @Reference(required = false)
    public void setProviders(List<BindingProvider> providers) {
        this.providers = providers;
        orderProviders();
    }

    @Reference(required = false)
    public void setStrategy(BindingSelectionStrategy strategy) {
        this.strategy = strategy;
    }

    @Init
    public void orderProviders() {
        if (strategy != null) {
            strategy.order(providers);
        }
    }


    public void selectBindings(LogicalComponent<?> component) throws BindingSelectionException {
        for (LogicalReference reference : component.getReferences()) {
            for (LogicalWire wire : reference.getWires()) {
                if (wire.getTargetUri() != null) {
                    URI targetUri = UriHelper.getDefragmentedName(wire.getTargetUri());
                    LogicalComponent target = logicalComponentManager.getComponent(targetUri);
                    assert target != null;
                    if ((component.getZone() == null && target.getZone() == null)) {
                        // components are local, no need for a binding
                        continue;
                    } else if (component.getZone() != null && component.getZone().equals(target.getZone())) {
                        // components are local, no need for a binding
                        continue;
                    }
                    LogicalService targetServce = target.getService(wire.getTargetUri().getFragment());
                    assert targetServce != null;
                    selectBinding(reference, targetServce);
                }
            }
        }
    }

    /**
     * Selects and configures a binding to connect the source to the target.
     *
     * @param source the source reference
     * @param target the target reference
     * @throws BindingSelectionException if an error occurs selecting a binding
     */
    private void selectBinding(LogicalReference source, LogicalService target) throws BindingSelectionException {
        List<BindingMatchResult> results = new ArrayList<BindingMatchResult>();

        for (BindingProvider provider : providers) {
            BindingMatchResult result = provider.canBind(source, target);
            if (result.isMatch()) {
                provider.bind(source, target);
                return;
            }
            results.add(result);

        }
        URI sourceUri = source.getUri();
        URI targetUri = target.getUri();
        throw new NoSCABindingProviderException("No SCA binding provider suitable for creating wire from " + sourceUri + " to " + targetUri, results);
    }

}

