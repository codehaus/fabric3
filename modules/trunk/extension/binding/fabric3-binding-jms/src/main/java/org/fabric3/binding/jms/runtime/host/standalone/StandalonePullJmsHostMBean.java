/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.binding.jms.runtime.host.standalone;

import java.util.List;

import org.fabric3.api.annotation.Management;

/**
 * Management interface for the standalone pull JMS host.
 *
 * @version $Revision$ $Date$
 */
@Management
public interface StandalonePullJmsHostMBean {

    /**
     * Gets the number of listeners for a service.
     *
     * @param service the service URI.
     * @return listener count.
     */
    int getReceiverCount(String service);

    /**
     * Sets the number of listeners for a service.
     *
     * @param service the service URI.
     * @param count   the listener count.
     * @throws ConfigurationUpdateException if there was an error setting the receiver count
     */
    void setReceiverCount(String service, int count) throws ConfigurationUpdateException;

    /**
     * Returns the list of current services.
     *
     * @return All the current services.
     */
    List<String> getReceivers();

}
