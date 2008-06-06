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

import java.io.IOException;
import java.io.InputStream;

import org.fabric3.ftp.server.data.DataConnection;
import org.fabric3.ftp.server.data.PassiveDataConnection;
import org.fabric3.ftp.server.protocol.DefaultResponse;
import org.fabric3.ftp.server.protocol.FtpSession;
import org.fabric3.ftp.server.protocol.Request;
import org.fabric3.ftp.server.protocol.RequestHandler;

/**
 * Handles the <code>STOR</code> command.
 * 
 * TODO Add mechanism to register the FTPlet.
 *
 * @version $Revision$ $Date$
 */
public class StorRequestHandler implements RequestHandler {

    public void service(Request request) {
        
        FtpSession session = request.getSession();
        int passivePort = session.getPassivePort();
        
        if (0 == passivePort) {
            session.write(new DefaultResponse(503, "PASV must be issued first"));
        }
        
        String fileName = request.getArgument();
        if (null == fileName) {
            session.write(new DefaultResponse(501, "Syntax error in parameters or arguments"));
        }
        
        session.write(new DefaultResponse(150, "File status okay; about to open data connection")).awaitUninterruptibly();
        
        DataConnection dataConnection = new PassiveDataConnection(passivePort);
        try {
            dataConnection.open();
        } catch (IOException ex) {
            session.write(new DefaultResponse(425, "Can't open data connection"));
            return;
        }
        
        boolean success = false;
        try {
            InputStream in = dataConnection.getInputStream();
            // TODO Temporary
            int read = in.read();
            while (read != -1) {
                System.err.print(read);
                read = in.read();
            }
            success = true;
        } catch (IOException ex) {
            session.write(new DefaultResponse(426, "Data connection error"));
            return;
        }
        
        try {
            if (success) {
                session.write(new DefaultResponse(226, "Transfer complete"));
            }
        } finally {
            dataConnection.close();
        }

    }

}
