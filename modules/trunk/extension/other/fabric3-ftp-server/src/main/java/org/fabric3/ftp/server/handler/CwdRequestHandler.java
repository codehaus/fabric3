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

import org.osoa.sca.annotations.Reference;

import org.fabric3.ftp.server.protocol.DefaultResponse;
import org.fabric3.ftp.server.protocol.FtpSession;
import org.fabric3.ftp.server.protocol.Request;
import org.fabric3.ftp.server.protocol.RequestHandler;
import org.fabric3.ftp.spi.FtpLetContainer;

/**
 * Handles the <code>CWD</code> command.
 * <p/>
 *
 * @version $Revision$ $Date$
 */
public class CwdRequestHandler implements RequestHandler {
    private FtpLetContainer container;

    @Reference
    public void setContainer(FtpLetContainer container) {
        this.container = container;
    }

    /**
     * Services the <code>CWD</code> request.
     *
     * @param request Object the encapsuates the current FTP command.
     */
    public void service(Request request) {
        FtpSession session = request.getSession();
        if (!session.isAuthenticated()) {
            session.write(new DefaultResponse(530, "Access Denied"));
            return;
        }
        String directory = request.getArgument();
        if (!container.isRegistered(directory)) {
            session.write(new DefaultResponse(550, directory + ": No such file or directory"));
            return;
        }
        session.setCurrentDirectory(directory);
        session.write(new DefaultResponse(250, "CWD command successful"));

    }

}