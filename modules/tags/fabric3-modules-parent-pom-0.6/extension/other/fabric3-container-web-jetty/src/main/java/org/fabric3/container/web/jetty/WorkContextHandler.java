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
package org.fabric3.container.web.jetty;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.handler.HandlerWrapper;

import org.fabric3.container.web.spi.WebRequestTunnel;
import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;

/**
 * Processes incoming requests for the web application context, adding a WorkContext to the thread so it is associated to user code in the web app.
 *
 * @version $Revision$ $Date$
 */
public class WorkContextHandler extends HandlerWrapper {
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
        WorkContext oldContext = null;
        try {
            WorkContext workContext = new WorkContext();
            CallFrame frame = new CallFrame();
            workContext.addCallFrame(frame);
            oldContext = PojoWorkContextTunnel.setThreadWorkContext(workContext);
            WebRequestTunnel.setRequest(request);
            super.handle(target, request, response, dispatch);
        } finally {
            PojoWorkContextTunnel.setThreadWorkContext(oldContext);
            WebRequestTunnel.setRequest(null);
        }
    }
}
