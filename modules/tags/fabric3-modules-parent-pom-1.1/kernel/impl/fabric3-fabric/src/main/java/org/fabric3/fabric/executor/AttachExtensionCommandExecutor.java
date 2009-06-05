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
 * --- Original Apache License ---
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.fabric.executor;

import java.net.URI;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.AttachExtensionCommand;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.executor.CommandExecutor;
import org.fabric3.spi.executor.CommandExecutorRegistry;
import org.fabric3.spi.executor.ExecutionException;

/**
 * Executes AttachExtensionCommands.
 *
 * @version $Rev: 2878 $ $Date: 2008-02-23 18:42:09 +0000 (Sat, 23 Feb 2008) $
 */
@EagerInit
public class AttachExtensionCommandExecutor implements CommandExecutor<AttachExtensionCommand> {

    private HostInfo info;
    private CommandExecutorRegistry commandExecutorRegistry;
    private ClassLoaderRegistry classLoaderRegistry;

    @Constructor
    public AttachExtensionCommandExecutor(@Reference HostInfo info,
                                          @Reference CommandExecutorRegistry commandExecutorRegistry,
                                          @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.info = info;
        this.commandExecutorRegistry = commandExecutorRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Init
    public void init() {
        commandExecutorRegistry.register(AttachExtensionCommand.class, this);
    }

    public void execute(AttachExtensionCommand command) throws ExecutionException {
        if (!info.supportsClassLoaderIsolation()) {
            return;
        }
        URI contributionUri = command.getContribution();
        URI providerUri = command.getProvider();
        // note: casts are safe as all extension and provider classloaders are multi-parent
        MultiParentClassLoader contributionCl = (MultiParentClassLoader) classLoaderRegistry.getClassLoader(contributionUri);
        MultiParentClassLoader providerCl = (MultiParentClassLoader) classLoaderRegistry.getClassLoader(providerUri);
        contributionCl.addExtensionClassLoader(providerCl);
    }
}