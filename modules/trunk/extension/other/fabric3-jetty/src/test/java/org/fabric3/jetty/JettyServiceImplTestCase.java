/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.jetty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import org.easymock.IAnswer;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.work.DefaultPausableWork;
import org.fabric3.host.work.WorkScheduler;

/**
 * @version $Rev$ $Date$
 */
public class JettyServiceImplTestCase extends TestCase {

    private static final String REQUEST1_HEADER =
            "GET / HTTP/1.0\n"
                    + "Host: localhost\n"
                    + "Content-Type: text/xml\n"
                    + "Connection: close\n"
                    + "Content-Length: ";
    private static final String REQUEST1_CONTENT =
            "";
    private static final String REQUEST1 =
            REQUEST1_HEADER + REQUEST1_CONTENT.getBytes().length + "\n\n" + REQUEST1_CONTENT;

    private static final int HTTP_PORT = 8585;

    private TransportMonitor monitor;
    private WorkScheduler scheduler;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private JettyServiceImpl service;

    /**
     * Verifies requests are properly routed according to the servlet mapping
     */
    public void testRegisterServletMapping() throws Exception {
        service.setHttpPort(String.valueOf(HTTP_PORT));
        service.init();
        TestServlet servlet = new TestServlet();
        service.registerMapping("/", servlet);
        Socket client = new Socket("127.0.0.1", HTTP_PORT);
        OutputStream os = client.getOutputStream();
        os.write(REQUEST1.getBytes());
        os.flush();
        read(client);
        service.destroy();
        assertTrue(servlet.invoked);
    }

//    public void testRequestSession() throws Exception {
//        JettyServiceImpl service = new JettyServiceImpl(monitor, scheduler);
//        service.setDebug(true);
//        service.setHttpPort(HTTP_PORT);
//        service.init();
//        TestServlet servlet = new TestServlet();
//        service.registerMapping("/", servlet);
//        Socket client = new Socket("127.0.0.1", HTTP_PORT);
//        OutputStream os = client.getOutputStream();
//        os.write(REQUEST1.getBytes());
//        os.flush();
//        read(client);
//        service.destroy();
//        assertTrue(servlet.invoked);
//        assertNotNull(servlet.sessionId);
//    }
//
//    public void testUseWorkScheduler() throws Exception {
//        JettyServiceImpl service = new JettyServiceImpl(monitor, scheduler);
//        service.setDebug(true);
//        service.setHttpPort(HTTP_PORT);
//        service.init();
//        TestServlet servlet = new TestServlet();
//        service.registerMapping("/", servlet);
//        Socket client = new Socket("127.0.0.1", HTTP_PORT);
//        OutputStream os = client.getOutputStream();
//        os.write(REQUEST1.getBytes());
//        os.flush();
//        read(client);
//        service.destroy();
//        assertTrue(servlet.invoked);
//    }

    public void testRestart() throws Exception {
        service.setHttpPort(String.valueOf(HTTP_PORT));
        service.init();
        service.destroy();
        service.init();
        service.destroy();
    }

    public void testNoMappings() throws Exception {
        service.setHttpPort(String.valueOf(HTTP_PORT));
        service.init();
        Socket client = new Socket("127.0.0.1", HTTP_PORT);
        OutputStream os = client.getOutputStream();
        os.write(REQUEST1.getBytes());
        os.flush();
        read(client);
        service.destroy();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        //executor.submit();
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        monitor = createMock(TransportMonitor.class);
        scheduler = createMock(WorkScheduler.class);
        scheduler.scheduleWork(isA(DefaultPausableWork.class));

        expectLastCall().andStubAnswer(new IAnswer() {
            public Object answer() throws Throwable {
                Runnable runnable = (Runnable) getCurrentArguments()[0];
                executor.execute(runnable);
                return null;
            }
        });
        replay(scheduler);
        service = new JettyServiceImpl(monitor);

    }

    private static String read(Socket socket) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private class TestServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;
        boolean invoked;
        String sessionId;

        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            invoked = true;
            sessionId = req.getSession().getId();
            OutputStream writer = resp.getOutputStream();
            try {
                writer.write("result".getBytes());
            } finally {
                writer.close();
            }
        }


    }
}
