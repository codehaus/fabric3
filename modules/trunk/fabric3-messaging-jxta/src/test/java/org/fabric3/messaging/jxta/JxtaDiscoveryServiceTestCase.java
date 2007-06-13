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
package org.fabric3.messaging.jxta;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import net.jxta.platform.NetworkConfigurator;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.messaging.RequestListener;
import org.fabric3.spi.services.messaging.ResponseListener;
import org.fabric3.spi.services.work.NotificationListener;
import org.fabric3.spi.services.work.WorkScheduler;
import org.fabric3.spi.util.stax.StaxUtil;

/**
 * @version $Revision$ $Date$
 */
public class JxtaDiscoveryServiceTestCase extends TestCase {

    public JxtaDiscoveryServiceTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testStartAndStop() throws Exception {

        JxtaMessagingService discoveryService = getMessagingService("runtime-1", "domain");

        discoveryService.joinDomain(-1);

        RequestListener requestListener = new RequestListener() {
            public XMLStreamReader onRequest(XMLStreamReader content) {
                try {
                    return StaxUtil.createReader("<response/>");
                } catch (XMLStreamException ex) {
                    throw new JxtaException(ex);
                }
            }
        };

        ResponseListener responseListener = new ResponseListener() {
            public void onResponse(XMLStreamReader content, int messageId) {
            }

        };

        discoveryService.registerRequestListener(new QName("request"), requestListener);
        discoveryService.registerResponseListener(new QName("response"), responseListener);

        XMLStreamReader reader = StaxUtil.createReader("<request/>");
        discoveryService.sendMessage(null, reader);
        reader.close();

    }

    private JxtaMessagingService getMessagingService(final String runtimeId, final String domain) {

        JxtaMessagingService messagingService = new JxtaMessagingService();
        HostInfo runtimeInfo = new HostInfo() {
            public File getApplicationRootDirectory() {
                return null;
            }

            public URL getBaseURL() {
                return null;
            }

            public URI getDomain() {
                try {
                    return new URI(domain);
                } catch (URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            }

            public String getRuntimeId() {
                return runtimeId;
            }

            public boolean isOnline() {
                return false;
            }

        };
        messagingService.setRuntimeInfo(runtimeInfo);

        NetworkConfigurator configurator = new NetworkConfigurator();
        configurator.setPrincipal("test-user");
        configurator.setPassword("test-password");

        messagingService.setConfigurator(configurator);
        messagingService.setWorkScheduler(new WorkScheduler() {
            public <T extends Runnable> void scheduleWork(final T work, final NotificationListener<T> listener) {
                new Thread() {
                    public void run() {
                        try {
                            work.run();
                        } catch (Exception ex) {
                            listener.workFailed(work, ex);
                        }
                    }
                }.start();
            }

            public <T extends Runnable> void scheduleWork(final T work) {
                new Thread() {
                    public void run() {
                        work.run();
                    }
                }.start();
            }
        });
        return messagingService;

    }

}
