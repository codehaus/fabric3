/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
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
 * @version $Rev$ $Date$
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
