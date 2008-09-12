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
package org.fabric3.binding.ftp.provision;

import java.net.URI;

import org.fabric3.binding.ftp.common.Constants;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * @version $Revision$ $Date$
 */
public class FtpWireTargetDefinition extends PhysicalWireTargetDefinition {
    private final URI classLoaderId;
    private final boolean active;
    private final FtpSecurity security;
    private int timeout;

    /**
     * Initializes the classloader id and transfer mode.
     *
     * @param classLoaderId the classloader id to deserialize parameters in
     * @param active        FTP transfer mode
     * @param security      Security parameters
     */
    public FtpWireTargetDefinition(URI classLoaderId, boolean active, FtpSecurity security) {
        this(classLoaderId, active, security, Constants.NO_TIMEOUT);
    }

    /**
     * Initializes the classloader id, transfer mode, and timeout.
     *
     * @param classLoaderId the classloader id to deserialize parameters in
     * @param active        FTP transfer mode
     * @param security      Security parameters
     * @param timeout       the timeout to use for blocking connection operations
     */
    public FtpWireTargetDefinition(URI classLoaderId, boolean active, FtpSecurity security, int timeout) {
        this.classLoaderId = classLoaderId;
        this.active = active;
        this.security = security;
        this.timeout = timeout;
    }

    /**
     * Returns the classloader id to deserialize parameters in.
     *
     * @return the classloader id to deserialize parameters in
     */
    public URI getClassLoaderId() {
        return classLoaderId;
    }

    /**
     * Gets the FTP transfer mode.
     *
     * @return True if user wants active transfer mode.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Get the security parameters.
     *
     * @return Get the security parameters.
     */
    public FtpSecurity getSecurity() {
        return security;
    }


    /**
     * Returns the timeout value to use for blocking operations or {@link Constants#NO_TIMEOUT} if none is set.
     *
     * @return the timeout value to use for blocking operations or {@link Constants#NO_TIMEOUT} if none is set
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout value to use for blocking operations.
     *
     * @param timeout the timeout value
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
