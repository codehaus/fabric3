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

import java.io.IOException;
import java.net.InetAddress;

import junit.framework.TestCase;

import org.apache.commons.net.ftp.FTPClient;
import org.fabric3.ftp.server.codec.CodecFactory;

/**
 *
 * @version $Revision$ $Date$
 */
public class F3FtpHostTest extends TestCase {

    public void testConnect() throws IOException {
        
        F3FtpHost ftpHost = new F3FtpHost();
        FtpHandler ftpHandler = new FtpHandler();
        ftpHost.setFtpHandler(ftpHandler);
        ftpHost.setCommandPort(1234);
        ftpHost.setCodecFactory(new CodecFactory());
        ftpHost.start();
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(InetAddress.getLocalHost(), 1234);
        ftpHost.stop();
        
    }

}
