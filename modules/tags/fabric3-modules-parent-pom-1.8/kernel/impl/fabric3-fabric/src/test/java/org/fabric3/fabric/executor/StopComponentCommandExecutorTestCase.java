/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.command.StartComponentCommand;
import org.fabric3.fabric.command.StopComponentCommand;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.executor.CommandExecutorRegistry;

/**
 * @version $Rev: 10102 $ $Date: 2011-03-15 23:59:22 -0700 (Tue, 15 Mar 2011) $
 */
public class StopComponentCommandExecutorTestCase extends TestCase {

    public void testExecute() throws Exception {
        CommandExecutorRegistry executorRegistry = EasyMock.createMock(CommandExecutorRegistry.class);
        ComponentManager manager = EasyMock.createMock(ComponentManager.class);
        executorRegistry.register(EasyMock.eq(StopComponentCommand.class), EasyMock.isA(StopComponentCommandExecutor.class));
        Component component = EasyMock.createMock(Component.class);
        component.stop();
        EasyMock.expect(manager.getComponent(EasyMock.isA(URI.class))).andReturn(component);
        EasyMock.replay(executorRegistry, manager, component);

        StopComponentCommandExecutor executor = new StopComponentCommandExecutor(manager, executorRegistry);
        executor.init();
        StopComponentCommand command = new StopComponentCommand(URI.create("component"));
        executor.execute(command);
        EasyMock.verify(executorRegistry, manager, component);
    }

}
