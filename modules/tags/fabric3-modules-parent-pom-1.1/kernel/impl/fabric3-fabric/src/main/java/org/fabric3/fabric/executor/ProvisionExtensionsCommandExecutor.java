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
package org.fabric3.fabric.executor;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.ProvisionExtensionsCommand;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.spi.contribution.ContributionUriResolver;
import org.fabric3.spi.contribution.ResolutionException;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;

/**
 * Provisions and installs extension contributions.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class ProvisionExtensionsCommandExecutor implements CommandExecutor<ProvisionExtensionsCommand> {
    private CommandExecutorRegistry commandExecutorRegistry;
    private ContributionService contributionService;
    private Domain domain;
    private Map<String, ContributionUriResolver> resolvers;

    public ProvisionExtensionsCommandExecutor(@Reference CommandExecutorRegistry commandExecutorRegistry,
                                              @Reference ContributionService contributionService,
                                              @Reference(name = "domain") Domain domain) {
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.contributionService = contributionService;
        this.domain = domain;
    }

    /**
     * Lazily injects the contribution URI resolvers that may be supplied by extensions.
     *
     * @param resolvers the resolvers keyed by URI scheme
     */
    @Reference
    public void setContributionUriResolver(Map<String, ContributionUriResolver> resolvers) {
        this.resolvers = resolvers;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(ProvisionExtensionsCommand.class, this);
    }

    public void execute(ProvisionExtensionsCommand command) throws ExecutionException {
        try {
            List<URI> stored = new ArrayList<URI>();
            for (URI encoded : command.getExtensionUris()) {
                ContributionUriResolver resolver = getResolver(encoded);
                URI uri = resolver.decode(encoded);
                if (contributionService.exists(uri)) {
                    // extension already provisioned
                    continue;
                }
                URL url = resolver.resolve(encoded);
                ContributionSource source = new FileContributionSource(uri, url, 0, new byte[]{});
                contributionService.store(source);
                stored.add(uri);
            }
            if (stored.isEmpty()) {
                return;
            }
            contributionService.install(stored);
            domain.include(stored, false);
        } catch (ResolutionException e) {
            throw new ExecutionException(e);
        } catch (ContributionException e) {
            throw new ExecutionException(e);
        } catch (DeploymentException e) {
            throw new ExecutionException(e);
        }
    }

    private ContributionUriResolver getResolver(URI uri) throws ExecutionException {
        String scheme = uri.getScheme();
        if (scheme == null) {
            scheme = ContributionUriResolver.LOCAL_SCHEME;
        }
        ContributionUriResolver resolver = resolvers.get(scheme);
        if (resolver == null) {
            throw new ExecutionException("Contribution resolver for scheme not found: " + scheme);
        }
        return resolver;
    }

}
