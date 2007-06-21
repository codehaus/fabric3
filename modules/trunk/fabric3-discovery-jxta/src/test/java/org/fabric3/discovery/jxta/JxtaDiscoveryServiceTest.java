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
import java.util.Collections;
import java.util.Set;

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import net.jxta.platform.NetworkConfigurator;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.jxta.impl.JxtaServiceImpl;
import org.fabric3.spi.services.advertisement.AdvertisementListener;
import org.fabric3.spi.services.advertisement.AdvertisementService;
import org.fabric3.spi.services.work.NotificationListener;
import org.fabric3.spi.services.work.WorkScheduler;

/**
 * @version $Revsion$ $Date$
 */
public class JxtaDiscoveryServiceTest extends TestCase {

    public void testGetParticipatingRuntimes() throws Exception {

        JxtaDiscoveryService discoveryService = new JxtaDiscoveryService();

        HostInfo hostInfo = new MyHostInfo(new URI("domain"), "runtime2");

        NetworkConfigurator configurator = new NetworkConfigurator();
        configurator.setPrincipal("test-user");
        configurator.setPassword("test-password");

        JxtaServiceImpl jxtaService = new JxtaServiceImpl();
        jxtaService.setHostInfo(hostInfo);
        jxtaService.setNetworkConfigurator(configurator);

        WorkScheduler workScheduler = new MyWorkScheduler();
        AdvertisementService advertisementService = new MyAdvertisementService();

        discoveryService.setWorkScheduler(workScheduler);
        discoveryService.setHostInfo(hostInfo);
        discoveryService.setAdvertisementService(advertisementService);
        discoveryService.setJxtaService(jxtaService);

        jxtaService.start();
        discoveryService.joinDomain(-1);

        Thread.sleep(50000);

        discoveryService.stop();

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
        private String runtimeId;

        public MyHostInfo(URI domain, String runtimeId) {
            this.domain = domain;
            this.runtimeId = runtimeId;
        }

        public URL getBaseURL() {
            return null;
        }

        public URI getDomain() {
            return domain;
        }

        public String getRuntimeId() {
            return runtimeId;
        }

        public boolean isOnline() {
            return false;
        }

    }

    private class MyAdvertisementService implements AdvertisementService {

        public void addFeature(QName qname) {
        }

        public void addListener(AdvertisementListener listener) {
        }

        public Set<QName> getFeatures() {
            return Collections.EMPTY_SET;
        }

        public void removeFeature(QName qname) {
        }

        public void removeListener(AdvertisementListener listener) {
        }

    }

}
