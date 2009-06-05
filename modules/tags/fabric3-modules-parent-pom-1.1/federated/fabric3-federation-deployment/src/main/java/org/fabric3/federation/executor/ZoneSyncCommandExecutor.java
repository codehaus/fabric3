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
package org.fabric3.federation.executor;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.federation.command.ZoneSyncCommand;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;

/**
 * Processes a ZoneSyncCommand on the controller by regenerating a set of deployment commands for the current state of the zone.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class ZoneSyncCommandExecutor implements CommandExecutor<ZoneSyncCommand> {
    private Domain domain;
    private CommandExecutorRegistry executorRegistry;

    public ZoneSyncCommandExecutor(@Reference(name = "domain") Domain domain, @Reference CommandExecutorRegistry executorRegistry) {
        this.domain = domain;
        this.executorRegistry = executorRegistry;
    }

    @Init
    public void init() {
        executorRegistry.register(ZoneSyncCommand.class, this);
    }

    public void execute(ZoneSyncCommand command) throws ExecutionException {
        try {
            domain.regenerate(command.getZoneId(), command.getRuntimeId());
        } catch (DeploymentException e) {
            throw new ExecutionException(e);
        }
    }
}