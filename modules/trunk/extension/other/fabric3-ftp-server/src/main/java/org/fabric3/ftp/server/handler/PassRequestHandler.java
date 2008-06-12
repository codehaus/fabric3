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
package org.fabric3.ftp.server.handler;

import org.fabric3.ftp.server.protocol.DefaultResponse;
import org.fabric3.ftp.server.protocol.FtpSession;
import org.fabric3.ftp.server.protocol.Request;
import org.fabric3.ftp.server.protocol.RequestHandler;
import org.fabric3.ftp.server.security.User;
import org.fabric3.ftp.server.security.UserManager;
import org.osoa.sca.annotations.Reference;

/**
 * Handles the <code>PASS</code> command.
 * 
 * @version $Revision$ $Date$
 */
public class PassRequestHandler implements RequestHandler {
    
    private UserManager userManager;
    
    /**
     * Injects the user manager.
     * 
     * @param userManager Injects the user manager.
     */
    @Reference
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public void service(Request request) {
        
        FtpSession session = request.getSession();
        User user = session.getUser();
        
        if (user == null) {
            session.write(new DefaultResponse(503, "Login with USER first"));
        }
        
        String userName = user.getName();
        String password = request.getArgument();
        
        if (password == null) {
            session.write(new DefaultResponse(501, "Syntax error in parameters or arguments"));
        }
        
        if (userManager.login(userName, password)) {
            session.setAuthenticated();
            session.write(new DefaultResponse(230, "User logged in, proceed"));
        } else {
            session.write(new DefaultResponse(530, "Authentication failed"));
        }

    }

}
