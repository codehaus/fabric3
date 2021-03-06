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
package org.fabric3.ftp.spi;

import org.fabric3.ftp.api.FtpLet;

/**
 * SPI for the FTP let container.
 *
 * @version $Revision$ $Date$
 */
public interface FtpLetContainer {

    /**
     * Registers an FTP let for the specified path.
     *
     * @param path   Path on which the FtpLet is listening.
     * @param ftpLet FtpLet listening for the upload request.
     */
    void registerFtpLet(String path, FtpLet ftpLet);

    /**
     * Gets a registered FTP let for the file name.
     *
     * @param fileName Fully qualified name for the file name.
     * @return FTP let that is registered, null if none registered.
     */
    FtpLet getFtpLet(String fileName);

    /**
     * Returns true if an FtpLet is registered for the given path.
     *
     * @param path the path.
     * @return true if an FtpLet is registered for the given path
     */
    public boolean isRegistered(String path);


}
