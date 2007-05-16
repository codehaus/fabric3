/*
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
package org.fabric3.runtime.standalone.host;

import java.io.File;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Constructor;

import org.fabric3.spi.loader.IllegalSCDLNameException;
import org.fabric3.spi.services.VoidService;

import org.fabric3.fabric.services.extension.AbstractExtensionDeployer;
import org.fabric3.runtime.standalone.StandaloneHostInfo;

/**
 * Service that extends the runtime by loading composites located in a directory.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class DirectoryScanExtender extends AbstractExtensionDeployer implements VoidService {
    private final StandaloneHostInfo hostInfo;
    private final String path;

    public DirectoryScanExtender(@Reference StandaloneHostInfo hostInfo,
                                 @Property(name = "path")String path) {
        this.hostInfo = hostInfo;
        this.path = path;
    }

    /**
     * @Deprecated JFM FIXME remove when properties working
     */
    @Constructor
    public DirectoryScanExtender(@Reference StandaloneHostInfo hostInfo) {
        this.hostInfo = hostInfo;
        this.path = "extensions";
    }

    @Init
    public void init() throws IllegalSCDLNameException {
        assert hostInfo != null;
        File extensionDir = new File(hostInfo.getInstallDirectory(), path);
        if (!extensionDir.isDirectory()) {
            // we don't have an extension directory, there's nothing to do
            return;
        }

        File[] files = extensionDir.listFiles();
        for (File file : files) {
            deployExtension(file);
        }
    }
}
