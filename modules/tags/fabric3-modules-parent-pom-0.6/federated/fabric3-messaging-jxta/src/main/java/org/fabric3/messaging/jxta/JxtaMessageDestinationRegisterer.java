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

import net.jxta.peer.PeerID;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.jxta.JxtaService;
import org.fabric3.spi.services.VoidService;
import org.fabric3.spi.services.runtime.RuntimeInfoService;

/**
 * Registers the message destination for the current runtime with the RuntimeInfoService.
 *
 * @version $Revsion$ $Date$
 */
@EagerInit
public class JxtaMessageDestinationRegisterer implements VoidService {
    private RuntimeInfoService runtimeInfoService;
    private JxtaService jxtaService;

    public JxtaMessageDestinationRegisterer(@Reference RuntimeInfoService runtimeInfoService,
                                            @Reference JxtaService jxtaService) {
        this.runtimeInfoService = runtimeInfoService;
        this.jxtaService = jxtaService;
    }

    @Init
    public void init() {
        PeerID peerID = jxtaService.getDomainGroup().getPeerID();
        runtimeInfoService.registerMessageDestination(peerID.toString());
    }

}
