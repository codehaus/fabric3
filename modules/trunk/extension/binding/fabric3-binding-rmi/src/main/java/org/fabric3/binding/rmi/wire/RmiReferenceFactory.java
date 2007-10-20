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
package org.fabric3.binding.rmi.wire;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.osoa.sca.ServiceUnavailableException;

public class RmiReferenceFactory {

    private final String host;
    private final int port;
    private final String serviceName;

    /* package */ RmiReferenceFactory(String serviceName, String host, int port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
    }

    public Object getReference() {
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(host, port);
            return registry.lookup(serviceName);
        } catch (RemoteException e) {
            throw new ServiceUnavailableException(serviceName, e);
        } catch (NotBoundException e) {
            throw new ServiceUnavailableException(serviceName, e);
        }
    }


}

