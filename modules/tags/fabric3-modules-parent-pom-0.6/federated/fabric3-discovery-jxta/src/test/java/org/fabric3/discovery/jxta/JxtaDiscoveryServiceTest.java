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
package org.fabric3.discovery.jxta;

import java.io.File;
import java.net.URI;

import junit.framework.TestCase;
import net.jxta.platform.NetworkConfigurator;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.work.DefaultPausableWork;
import org.fabric3.host.work.WorkScheduler;
import org.fabric3.jxta.impl.JxtaServiceImpl;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.runtime.RuntimeInfoService;

/**
 * @version $Revsion$ $Date$
 */
public class JxtaDiscoveryServiceTest extends TestCase {

    public void testGetParticipatingRuntimes() throws Exception {

        URI runtimeId = URI.create("runtime2");

        JxtaDiscoveryService discoveryService = new JxtaDiscoveryService();

        HostInfo hostInfo = new MyHostInfo(new URI("domain"));

        NetworkConfigurator configurator = new NetworkConfigurator();
        configurator.setPrincipal("test-user");
        configurator.setPassword("test-password");

        JxtaServiceImpl jxtaService = new JxtaServiceImpl();
        jxtaService.setHostInfo(hostInfo);
        jxtaService.setNetworkConfigurator(configurator);

        WorkScheduler workScheduler = new MyWorkScheduler();
        RuntimeInfoService runtimeInfoService = new MyRuntimeInfoService(runtimeId);

        discoveryService.setWorkScheduler(workScheduler);
        discoveryService.setRuntimeInfoService(runtimeInfoService);
        discoveryService.setJxtaService(jxtaService);

        jxtaService.start();
        discoveryService.joinDomain(-1);

        Thread.sleep(100000);

        discoveryService.stop();
        //System.exit(0);

    }

    private class MyWorkScheduler implements WorkScheduler {

		public <T extends DefaultPausableWork> void scheduleWork(T work) {
            new Thread(work).start();
		}

    }

    private class MyHostInfo implements HostInfo {

        private URI domain;

        public MyHostInfo(URI domain) {
            this.domain = domain;
        }

        public File getBaseDir() {
            return null;
        }

        public URI getDomain() {
            return domain;
        }

        public boolean isOnline() {
            return false;
        }

        public String getProperty(String name, String defaultValue) {
            return null;
        }

        public boolean supportsClassLoaderIsolation() {
            return true;
        }

    }

    private class MyRuntimeInfoService implements RuntimeInfoService {

        private URI runtimeId;

        public MyRuntimeInfoService(URI runtimeId) {
            this.runtimeId = runtimeId;
        }

        public URI getCurrentRuntimeId() {
            return runtimeId;
        }

        public RuntimeInfo getRuntimeInfo() {
            return new RuntimeInfo(runtimeId);
        }

        public void registerMessageDestination(String id) {

        }

    }

}
