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

import java.util.List;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.fabric3.spi.ObjectCreationException;
import org.fabric3.pojo.reflection.Injector;

/**
 * Injects reference proxies into an HTTP session when it is created.
 *
 * @version $Revision$ $Date$
 */
public class InjectingSessionListener implements HttpSessionListener {
    private List<Injector<HttpSession>> injectors;

    public InjectingSessionListener(List<Injector<HttpSession>> injectors) {
        this.injectors = injectors;
    }

    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        for (Injector<HttpSession> injector : injectors) {
            try {
                injector.inject(session);
            } catch (ObjectCreationException e) {
                throw new RuntimeInjectionException(e);
            }
        }
    }

    public void sessionDestroyed(HttpSessionEvent se) {

    }
}
