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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.UnProvisionExtensionsCommand;
import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.RemoveException;
import org.fabric3.host.contribution.UninstallException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.domain.UndeploymentException;
import org.fabric3.spi.contribution.ContributionUriResolver;
import org.fabric3.spi.contribution.ResolutionException;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;

/**
 * Undeploys, uninstalls, and removes extension contributions from a runtime.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class UnProvisionExtensionsCommandExecutor implements CommandExecutor<UnProvisionExtensionsCommand> {
    private CommandExecutorRegistry commandExecutorRegistry;
    private ContributionService contributionService;
    private Domain domain;
    private Map<String, ContributionUriResolver> resolvers;

    public UnProvisionExtensionsCommandExecutor(@Reference CommandExecutorRegistry commandExecutorRegistry,
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
        commandExecutorRegistry.register(UnProvisionExtensionsCommand.class, this);
    }


    public synchronized void execute(UnProvisionExtensionsCommand command) throws ExecutionException {
        // compile the list of extensions 
        List<URI> uninstall = new ArrayList<URI>();
        for (URI encoded : command.getExtensionUris()) {
            ContributionUriResolver resolver = getResolver(encoded);
            URI uri = resolver.decode(encoded);
            int count = resolver.getInUseCount(uri);
            if (count == 1) {
                try {
                    // no longer in use, undeploy and uninstall the extension
                    List<Deployable> deployables = contributionService.getDeployables(uri);
                    List<Deployable> reverse = new ArrayList<Deployable>(deployables);
                    // undeploy in reverse order
                    Collections.reverse(reverse);
                    for (Deployable deployable : reverse) {
                        domain.undeploy(deployable.getName());
                    }
                    uninstall.add(uri);
                } catch (UndeploymentException e) {
                    throw new ExecutionException(e);
                } catch (ContributionNotFoundException e) {
                    throw new ExecutionException(e);
                }
            }
            try {
                resolver.release(uri);
            } catch (ResolutionException e) {
                throw new ExecutionException(e);
            }
        }
        try {
            contributionService.uninstall(uninstall);
            contributionService.remove(uninstall);
        } catch (ContributionNotFoundException e) {
            throw new ExecutionException(e);
        } catch (RemoveException e) {
            throw new ExecutionException(e);
        } catch (UninstallException e) {
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