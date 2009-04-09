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
package org.fabric3.fabric.generator.extension;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.AbstractExtensionsCommand;
import org.fabric3.fabric.command.ProvisionExtensionsCommand;
import org.fabric3.fabric.command.UnProvisionExtensionsCommand;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.AbstractComponentType;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionUriEncoder;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * @version $Revision$ $Date$
 */
public class ExtensionGeneratorImpl implements ExtensionGenerator {
    private MetaDataStore store;
    private HostInfo info;
    private ContributionUriEncoder encoder;

    public ExtensionGeneratorImpl(@Reference MetaDataStore store, @Reference HostInfo info) {
        this.store = store;
        this.info = info;
    }

    /**
     * Setter for injecting the service for encoding contribution URIs so they may be derferenced in a domain. This is done lazily as the encoder is
     * supplied by an extension which is intialized after this component which is needed during bootstrap.
     *
     * @param encoder the encoder to inject
     */
    @Reference(required = false)
    public void setEncoder(ContributionUriEncoder encoder) {
        this.encoder = encoder;
    }

    public Map<String, Command> generate(Map<String, List<Contribution>> contributions, List<LogicalComponent<?>> components, boolean provision)
            throws GenerationException {
        if (RuntimeMode.CONTROLLER != info.getRuntimeMode()) {
            // short circuit this unless running in distributed mode
            return null;
        }
        Map<String, Command> commands = new HashMap<String, Command>();
        for (Map.Entry<String, List<Contribution>> entry : contributions.entrySet()) {
            String zone = entry.getKey();
            if (zone == null) {
                // skip local runtime
                continue;
            }
            AbstractExtensionsCommand command;
            if (provision) {
                command = new ProvisionExtensionsCommand();
            } else {
                command = new UnProvisionExtensionsCommand();
            }

            List<Contribution> zoneContributions = entry.getValue();
            Set<Contribution> extensions = new HashSet<Contribution>();
            for (Contribution contribution : zoneContributions) {
                Set<Contribution> required = store.resolveCapabilities(contribution);
                extensions.addAll(required);
            }
            for (Contribution extension : extensions) {
                URI encoded = encode(extension.getUri());
                command.addExtensionUri(encoded);
            }
            if (!command.getExtensionUris().isEmpty()) {
                commands.put(zone, command);
            }

        }
        // evaluate model objects for required capabilities
        for (LogicalComponent<?> component : components) {
            String componentZone = component.getZone();
            if (componentZone == null) {
                // skip local runtime
                continue;
            }
            AbstractExtensionsCommand command;
            command = (AbstractExtensionsCommand) commands.get(componentZone);    // safe cast
            if (command == null) {
                if (provision) {
                    command = new ProvisionExtensionsCommand();
                    commands.put(componentZone, command);
                } else {
                    command = new UnProvisionExtensionsCommand();
                    commands.put(componentZone, command);
                }

            }
            generate(component, command);
        }


        if (commands.isEmpty()) {
            return null;
        }
        return commands;
    }

    /**
     * Evaluates a component for required capabilities
     *
     * @param component the component
     * @param command   the command to update
     * @throws GenerationException if an exception during evaluation is encountered
     */
    private void generate(LogicalComponent<?> component, AbstractExtensionsCommand command) throws GenerationException {
        Implementation<?> impl = component.getDefinition().getImplementation();
        AbstractComponentType<?, ?, ?, ?> type = impl.getComponentType();
        Set<Contribution> extensions = new HashSet<Contribution>();
        for (String capability : type.getRequiredCapabilities()) {
            extensions.addAll(store.resolveCapability(capability));
        }
        for (String capability : impl.getRequiredCapabilities()) {
            extensions.addAll(store.resolveCapability(capability));
        }
        // evaluate services
        for (LogicalService service : component.getServices()) {
            for (LogicalBinding<?> binding : service.getBindings()) {
                for (String capability : binding.getDefinition().getRequiredCapabilities()) {
                    extensions.addAll(store.resolveCapability(capability));
                }
            }
        }
        // evaluate references
        for (LogicalReference reference : component.getReferences()) {
            for (LogicalBinding<?> binding : reference.getBindings()) {
                for (String capability : binding.getDefinition().getRequiredCapabilities()) {
                    extensions.addAll(store.resolveCapability(capability));
                }
            }
        }
        for (Contribution extension : extensions) {
            URI encoded = encode(extension.getUri());
            command.addExtensionUri(encoded);
        }
    }


    /**
     * Encodes a contribution URI to one that is derferenceable from a runtime in the domain
     *
     * @param uri the contribution URI
     * @return a URI that is derferenceable in the domain
     * @throws GenerationException if the URI cannot be encoded
     */
    private URI encode(URI uri) throws GenerationException {
        if (encoder != null) {
            try {
                return encoder.encode(uri);
            } catch (URISyntaxException e) {
                throw new GenerationException(e);
            }
        }
        return uri;


    }

}
