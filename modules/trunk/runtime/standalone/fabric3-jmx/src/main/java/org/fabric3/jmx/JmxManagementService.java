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
package org.fabric3.jmx;

import java.net.URI;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.fabric3.host.management.ManagementService;

/**
 * JMX implementation of the management service.
 *
 * @version $Revision$ $Date$
 */
public class JmxManagementService implements ManagementService {

    /**
     * MBean server used by the JMX management service.
     */
    private final MBeanServer mBeanServer;

    /**
     * Management domain used by the runtime.
     */
    private final String managementDomain;


    /**
     * Constructor that initializes the MBeanServer and domain to use for registering components.
     *
     * @param mBeanServer      the MBeanServer components should be registered with
     * @param managementDomain the JMX domain to use when generating ObjectNames
     */
    public JmxManagementService(MBeanServer mBeanServer, String managementDomain) {
        this.mBeanServer = mBeanServer;
        this.managementDomain = managementDomain;
    }

    public <T> void registerService(URI component, String service, Class<T> managementInterface, T instance) {
        try {
            ObjectName name = new ObjectName(managementDomain + ":type=service,component=\"" + component.toString() + "\",service=" + service);
            StandardMBean mbean = new StandardMBean(instance, managementInterface);
            mBeanServer.registerMBean(mbean, name);
        } catch (JMException e) {
            throw new JmxException(e);
        }
    }
}
