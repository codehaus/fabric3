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
package org.fabric3.jmx.agent.rmi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.management.remote.JMXServiceURL;

import org.fabric3.jmx.agent.AbstractAgent;
import org.fabric3.jmx.agent.ManagementException;

/**
 * Utility for starting the JMX server with an RMI agent.
 * 
 * @version $Revsion$ $Date: 2007-09-02 01:33:01 +0100 (Sun, 02 Sep 2007) $
 *
 */
public class RmiAgent extends AbstractAgent {
    
    private Registry registry;

    /**
     * Initializes the listen port.
     * @param minPort Listen port minimum value.
     * @param maxPort Listen port maximum value.
     * @throws ManagementException
     */
    public RmiAgent(int minPort, int maxPort) throws ManagementException {
        super(minPort, maxPort);
    }

    /**
     * Gets the adaptor URL.
     * @return Adaptor URL used by the agent.
     * @throws ManagementException If unable to start the agent.
     */
    protected JMXServiceURL getAdaptorUrl() throws ManagementException {
        
        try {
            return new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:" + getPort() + "/server");
            // service:jmx:rmi:///jndi/rmi://localhost:1099/server
        } catch (MalformedURLException ex) {
            throw new ManagementException(ex);
        }
        
    }

    /**
     * @see org.fabric3.jmx.agent.AbstractAgent#preStart()
     */
    @Override
    public void preStart() throws ManagementException {

        try {
            registry = LocateRegistry.createRegistry(getPort());
        } catch (RemoteException ex) {
            throw new ManagementException(ex);
        }

    }

    /**
     * @see org.fabric3.jmx.agent.AbstractAgent#postStop()
     */
    @Override
    public void postStop() throws ManagementException {
        
        try {
            UnicastRemoteObject.unexportObject(registry, true);            
        } catch (IOException ex) {
            throw new ManagementException(ex);
        }
        
    }

}
