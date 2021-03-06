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
package org.fabric3.ftp.server.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Active data connection.
 * 
 * @version $Revision$ $Date$
 */
public class ActiveDataConnection implements DataConnection {
    
    /**
     * Initializes a data connection.
     * 
     * @throws IOException If unable to open connection.
     */
    public void initialize() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Closes the data connection.
     */
    public void close() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get an input stream to the data connection.
     * 
     * @return Input stream to the data cnnection.
     * @throws IOException If unable to get input stream.
     */
    public InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get an output stream to the data connection.
     * 
     * @return Output stream to the data connection.
     * @throws IOException If unable to get output stream.
     */
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Opens the data connection.
     * 
     * @throws IOException If unable to open connection.
     */
    public void open() throws IOException {
        throw new UnsupportedOperationException();
    }

}
