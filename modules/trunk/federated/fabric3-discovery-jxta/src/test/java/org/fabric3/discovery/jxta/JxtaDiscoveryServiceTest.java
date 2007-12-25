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

import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;
import net.jxta.platform.NetworkConfigurator;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.jxta.impl.JxtaServiceImpl;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.runtime.RuntimeInfoService;
import org.fabric3.spi.services.work.NotificationListener;
import org.fabric3.spi.services.work.WorkScheduler;

/**
 * @version $Revsion$ $Date$
 */
public class JxtaDiscoveryServiceTest extends TestCase {

    public void testGetParticipatingRuntimes() throws Exception {

        URI runtimeId = URI.create("runtime2");

        JxtaDiscoveryService discoveryService = new JxtaDiscoveryService();

        HostInfo hostInfo = new MyHostInfo(new URI("domain"), runtimeId);

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

        public <T extends Runnable> void scheduleWork(T runnable) {
            new Thread(runnable).start();
        }

        public <T extends Runnable> void scheduleWork(T runnable, NotificationListener<T> arg1) {
            // TODO Auto-generated method stub

        }

    }

    private class MyHostInfo implements HostInfo {

        private URI domain;
        private URI runtimeId;

        public MyHostInfo(URI domain, URI runtimeId) {
            this.domain = domain;
            this.runtimeId = runtimeId;
        }

        public URL getBaseURL() {
            return null;
        }

        public URI getDomain() {
            return domain;
        }

        public URI getRuntimeId() {
            return runtimeId;
        }

        public boolean isOnline() {
            return false;
        }

        public String getProperty(String name, String defaultValue) {
            return null;
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

    }

}
