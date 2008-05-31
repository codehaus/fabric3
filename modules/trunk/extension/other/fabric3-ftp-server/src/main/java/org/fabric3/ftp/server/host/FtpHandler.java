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
package org.fabric3.ftp.server.host;

import java.util.Map;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.fabric3.ftp.server.protocol.DefaultRequest;
import org.fabric3.ftp.server.protocol.DefaultResponse;
import org.fabric3.ftp.server.protocol.FtpSession;
import org.fabric3.ftp.server.protocol.Request;
import org.fabric3.ftp.server.protocol.RequestHandler;
import org.fabric3.ftp.server.protocol.Response;
import org.osoa.sca.annotations.Reference;

/**
 *
 * @version $Revision$ $Date$
 */
public class FtpHandler implements IoHandler {
    
    private Map<String, RequestHandler> requestHandlers;

    /**
     * Injects the FTP commands.
     * @param ftpCommands FTP commands.
     */
    @Reference
    public void setFtpCommands(Map<String, RequestHandler> ftpCommands) {
        this.requestHandlers = ftpCommands;
    }

    public void exceptionCaught(IoSession session, Throwable throwable) throws Exception {
    }

    public void messageReceived(IoSession session, Object message) throws Exception {
        
        FtpSession ftpSession = new FtpSession(session);
        Request request = new DefaultRequest(message.toString(), ftpSession);
        
        RequestHandler requestHandler = requestHandlers.get(request.getCommand());
        Response response = requestHandler.service(request);
        
        session.write(response);
        
    }

    public void messageSent(IoSession session, Object arg1) throws Exception {
    }

    public void sessionClosed(IoSession session) throws Exception {
    }

    public void sessionCreated(IoSession session) throws Exception {
        Response response = new DefaultResponse(220, "Service ready for new user.");
        session.write(response);
    }

    public void sessionIdle(IoSession session, IdleStatus idleStatus) throws Exception {
    }

    public void sessionOpened(IoSession session) throws Exception {
    }

}
