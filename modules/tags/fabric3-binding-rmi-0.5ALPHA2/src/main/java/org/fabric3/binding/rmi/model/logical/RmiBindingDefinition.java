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
package org.fabric3.binding.rmi.model.logical;

import java.net.URI;

import org.fabric3.scdl.BindingDefinition;

public class RmiBindingDefinition extends BindingDefinition {

    private static final boolean DEBUG = false;
    private String name;
    private int port = 7701;
    private String host = "localhost";
    private String serviceName;

    public RmiBindingDefinition(URI targetUri) {
        super(targetUri, RmiBindingLoader.BINDING_QNAME);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String toString() {
        if (DEBUG) {
            StringBuilder sb = new StringBuilder("Name: ");
            sb.append(name).append(" Service Name: ").append(serviceName);
            sb.append(" Host: ").append(host).append(" Port: ").append(port);
            return sb.toString();
        }
        return name;
    }


    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

}
