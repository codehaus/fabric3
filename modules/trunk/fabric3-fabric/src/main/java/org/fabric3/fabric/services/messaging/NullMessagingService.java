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
package org.fabric3.fabric.services.messaging;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.extension.messaging.AbstractMessagingService;
import org.fabric3.spi.services.messaging.DomainJoinException;
import org.fabric3.spi.services.messaging.MessagingException;
import org.fabric3.spi.services.messaging.MessagingTimeoutException;

/**
 * A messaging service implementation fr a single node runtime
 *
 * @version $Rev$ $Date$
 */
public class NullMessagingService extends AbstractMessagingService {
    private Set<String> runtimes;

    public NullMessagingService() {
        runtimes = new HashSet<String>();
        runtimes.add("master");
    }

    public void joinDomain(long waitTime) throws DomainJoinException, MessagingTimeoutException {

    }

    public void leaveDomain() throws MessagingException {

    }

    public int sendMessage(String runtimeId, XMLStreamReader content) throws MessagingException {
        return 0;
    }

    public Set<String> getRuntimeIds() {
        return Collections.unmodifiableSet(runtimes);
    }
}
