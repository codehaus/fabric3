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
package org.fabric3.ftp.server.protocol;

/**
 * Default implementation of the FTP request.
 * 
 * @version $Revision$ $Date$
 */
public class DefaultRequest implements Request {
    
    private String command;
    private String argument;
    private FtpSession session;
    
    /**
     * Initializes the message, argument and the session.
     * 
     * @param message FTP command and argument.
     * @param session FTP session.
     */
    public DefaultRequest(String message, FtpSession session) {
        
        message = message.trim();
        int index = message.indexOf(" ");
        if (index != -1) {
            command = message.substring(0, index).toUpperCase();
            argument = message.substring(index + 1);
        } else {
            command = message.trim();
        }
        
        this.session = session;
        
    }
    
    /**
     * Gets the command for the FTP request.
     * 
     * @return FTP command.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Gets the argument for the FTP request.
     * 
     * @return FTP command argument.
     */
    public String getArgument() {
        return argument;
    }
    
    /**
     * Gets the session associated with the FTP request.
     * 
     * @return FTP session.
     */
    public FtpSession getSession() {
        return session;
    }

}
