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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.UnProvisionExtensionsCommand;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.domain.Domain;
import org.fabric3.spi.contribution.ContributionUriResolver;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class UnProvisionExtensionsCommandExecutor implements CommandExecutor<UnProvisionExtensionsCommand> {
    private CommandExecutorRegistry commandExecutorRegistry;
    private ContributionUriResolver contributionUriResolver;
    private ContributionService contributionService;
    private Domain domain;

    public UnProvisionExtensionsCommandExecutor(@Reference CommandExecutorRegistry commandExecutorRegistry,
                                                @Reference ContributionUriResolver contributionUriResolver,
                                                @Reference ContributionService contributionService,
                                                @Reference(name = "domain") Domain domain) {
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.contributionUriResolver = contributionUriResolver;
        this.contributionService = contributionService;
        this.domain = domain;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(UnProvisionExtensionsCommand.class, this);
    }


    public void execute(UnProvisionExtensionsCommand command) throws ExecutionException {

        for (URI encoded : command.getExtensionUris()) {

        }

    }
}