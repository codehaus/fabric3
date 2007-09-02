/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.tests.development;

import junit.framework.TestCase;

import org.fabric3.runtime.development.Domain;

import java.net.URL;
import java.io.File;

/**
 * @version $Rev$ $Date$
 */
public class EchoTest extends TestCase {
    private EchoService echoService;

    public void testEcho() {
        assertEquals("Hello", echoService.echo("Hello"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        Domain domain = new Domain();
        URL composite = getClass().getResource("/META-INF/echo.composite");
        domain.activate(composite);
        echoService = domain.connectTo(EchoService.class, "Echo");
    }
}
