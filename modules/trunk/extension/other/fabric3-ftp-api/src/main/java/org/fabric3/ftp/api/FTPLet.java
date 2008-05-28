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
package org.fabric3.ftp.api;

import java.io.InputStream;

/**
 * 
 * Interface for receiving FTP callbacks.
 * 
 * Note: The concept is borrowed from Apache MINA FTP Server.
 *
 * @version $Revision$ $Date$
 */
public interface FTPLet {
    
    /**
     * Callback when data is uploaded by the remote FTP client.
     * 
     * @param path Name of the file being uploaded.
     * @param uploadData Stream of data that is being uploaded.
     */
    void onUpload(String fileName, InputStream uploadData);

}
