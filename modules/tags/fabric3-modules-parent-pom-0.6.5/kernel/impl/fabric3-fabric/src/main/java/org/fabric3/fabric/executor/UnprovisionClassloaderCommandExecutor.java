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

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.fabric.command.UnprovisionClassloaderCommand;
import org.fabric3.fabric.builder.classloader.ClassLoaderBuilder;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.CommandExecutor;

@EagerInit
public class UnprovisionClassloaderCommandExecutor implements CommandExecutor<UnprovisionClassloaderCommand> {

    private ClassLoaderBuilder classLoaderBuilder;
    private CommandExecutorRegistry commandExecutorRegistry;

    public UnprovisionClassloaderCommandExecutor(@Reference CommandExecutorRegistry commandExecutorRegistry,
                                               @Reference ClassLoaderBuilder classLoaderBuilder) {
        this.classLoaderBuilder = classLoaderBuilder;
        this.commandExecutorRegistry = commandExecutorRegistry;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(UnprovisionClassloaderCommand.class, this);
    }


    public void execute(UnprovisionClassloaderCommand command) {
        classLoaderBuilder.destroy(command.getUri());
    }

}
