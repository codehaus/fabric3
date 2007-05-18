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
package org.fabric3.fabric.services.deployment.fs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.fabric.assembly.Assembly;
import org.fabric3.fabric.assembly.DistributedAssembly;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.spi.services.VoidService;

/**
 * Periodically scans a deployment directory for file-based contributions
 */
@Service(VoidService.class)
@EagerInit
public class DirectoryScanner {
    private final ContributionService contributionService;
    private final DirectoryScannerMonitor monitor;
    private final Assembly assembly;
    private DeploymentResourceFactory deploymentResourceFactory;
    private String path = "deploy";

    private long delay = 5000;
    private ScheduledExecutorService executor;

    public DirectoryScanner(@Reference ContributionService contributionService,
                            @Reference DistributedAssembly assembly,
                            @Property(name = "path")String path,
                            @Property(name = "delay")long delay,
                            @Monitor DirectoryScannerMonitor monitor) {
        this.contributionService = contributionService;
        this.assembly = assembly;
        this.path = path;
        this.delay = delay;
        this.monitor = monitor;
//        this.deploymentResourceFactory = deploymentResourceFactory;
    }


    @Constructor
    @Deprecated
    // JFM FIXME when properties are supported
    public DirectoryScanner(@Reference ContributionService contributionService,
                            @Reference Assembly assembly,
                            @Reference MonitorFactory factory) {
        this.contributionService = contributionService;
        this.assembly = assembly;
        this.monitor = factory.getMonitor(DirectoryScannerMonitor.class);
//        this.deploymentResourceFactory = deploymentResourceFactory;
    }

    @Init
    public void init() {
        executor = Executors.newSingleThreadScheduledExecutor();
        // TODO temporarily disabled
        // executor.scheduleWithFixedDelay(new Scanner(), 10, delay, TimeUnit.MILLISECONDS);
    }

    @Destroy
    public void destroy() {
        executor.shutdownNow();
    }

    private class Scanner implements Runnable {
        // previously contributed directories
        private Map<String, DeploymentResource> contributed = new HashMap<String, DeploymentResource>();
        private Map<String, DeploymentResource> cache = new HashMap<String, DeploymentResource>();

        public void run() {
            File extensionDir = new File(path);
            if (!extensionDir.isDirectory()) {
                // we don't have an extension directory, there's nothing to do
                return;
            }

            File[] files = extensionDir.listFiles();
            for (File file : files) {
                try {
                    if (file.isDirectory()) {
//                        int deployIndex = contributed.indexOf(file);
//                        if (deployIndex > 0) {
//                            File deployedFile = contributed.get(deployIndex);
//                            if (file.lastModified() <= deployedFile.lastModified()) {
//                                continue;
//                            }
//                        }
//                        // TODO contribute here assumes an update
//                        URI uri = contributionService.contribute(file.toURL());
//                        contributed.add(file);
                    } else {
                        String name = file.getName();
                        DeploymentResource contribution = cache.get(name);
                        if (contribution == null) {
                            DeploymentResource resource = deploymentResourceFactory.createResource(file);
                            if (resource == null) {
                                // not a known type, ignore
                                continue;
                            }
                            // cache the resource and wait to the next run to see if it has changed
                            cache.put(name, resource);
                        } else {
                            // cached
                            if (contribution.isChanged()) {
                                // contents are still being updated, wait until next run
                                continue;
                            }
                            cache.remove(name);
                            // check if it is in the store
                            contribution = contributed.get(name);
                            if (contribution == null) {
                                // add the contribution
//                                URI uri = contributionService.contribute(file.toURL());
//                                contributed.add(file);

                            }
                        }
                    }
//                } catch (ContributionException e) {
//                    monitor.contributionError(e);
                } catch (IOException e) {
                    monitor.contributionError(e);
                }
            }
        }
    }

}