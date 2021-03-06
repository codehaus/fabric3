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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.fabric.executor;

import java.net.URI;
import java.util.List;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.DisposeChannelsCommand;
import org.fabric3.spi.channel.ChannelManager;
import org.fabric3.spi.channel.RegistrationException;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

/**
 * Removes a set of channels defined in a composite on a runtime.
 *
 * @version $Rev: 8634 $ $Date: 2010-02-03 08:17:32 -0800 (Wed, 03 Feb 2010) $
 */
@EagerInit
public class DisposeChannelsCommandExecutor implements CommandExecutor<DisposeChannelsCommand> {
    private ChannelManager channelManager;
    private CommandExecutorRegistry executorRegistry;

    @Constructor
    public DisposeChannelsCommandExecutor(@Reference ChannelManager channelManager, @Reference CommandExecutorRegistry executorRegistry) {
        this.channelManager = channelManager;
        this.executorRegistry = executorRegistry;
    }

    @Init
    public void init() {
        executorRegistry.register(DisposeChannelsCommand.class, this);
    }

    public void execute(DisposeChannelsCommand command) throws ExecutionException {
        try {
            List<PhysicalChannelDefinition> definitions = command.getDefinitions();
            for (PhysicalChannelDefinition definition : definitions) {
                URI uri = definition.getUri();
                channelManager.unregister(uri);
            }
        } catch (RegistrationException e) {
            throw new ExecutionException(e.getMessage(), e);
        }
    }

}