package org.fabric3.fabric.executor;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.fabric.command.UnprovisionClassloaderCommand;
import org.fabric3.fabric.builder.classloader.ClassLoaderBuilder;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.CommandExecutor;

/**
 * @author Copyright (c) 2008 by BEA Systems. All Rights Reserved.
 */
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
