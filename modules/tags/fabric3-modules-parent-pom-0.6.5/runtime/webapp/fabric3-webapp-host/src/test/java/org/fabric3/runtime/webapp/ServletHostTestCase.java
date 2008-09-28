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
package org.fabric3.runtime.webapp;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.verify;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class ServletHostTestCase extends TestCase {

    public void testDispatch() throws Exception {
        HttpServletRequest req = createMock(HttpServletRequest.class);
        expect(req.getPathInfo()).andReturn("foo");
        replay(req);
        HttpServletResponse res = createMock(HttpServletResponse.class);
        Servlet servlet = createMock(Servlet.class);
        servlet.service(req, res);
        EasyMock.expectLastCall();
        replay(servlet);
        ServletHostImpl host = new ServletHostImpl(null);
        host.registerMapping("foo", servlet);
        host.service(req, res);
        verify(servlet);
    }

    public void testDuplicateRegistration() throws Exception {
        Servlet servlet = createMock(Servlet.class);
        ServletHostImpl host = new ServletHostImpl(null);
        host.registerMapping("foo", servlet);
        assertEquals(true, host.isMappingRegistered("foo"));
        assertEquals(false, host.isMappingRegistered("bar"));
        try {
            host.registerMapping("foo", servlet);
            fail();
        } catch (IllegalStateException e) {
            // expected
        }
    }

    public void testUnregister() throws Exception {
        HttpServletRequest req = createMock(HttpServletRequest.class);
        expect(req.getPathInfo()).andReturn("foo");
        replay(req);
        HttpServletResponse res = createMock(HttpServletResponse.class);
        Servlet servlet = createMock(Servlet.class);
        replay(servlet);
        ServletHostImpl host = new ServletHostImpl(null);
        host.registerMapping("foo", servlet);
        Servlet unregedServlet = host.unregisterMapping("foo");
        assertEquals(unregedServlet, servlet);
        try {
            host.service(req, res);
        } catch (IllegalStateException e) {
            // expected
        }
        verify(servlet);
    }

}
