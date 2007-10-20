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
package org.fabric3.fabric.services.contenttype;

import java.net.URL;
import java.util.Map;
import java.util.HashMap;

import junit.framework.TestCase;

import org.fabric3.spi.services.contenttype.ContentTypeResolver;

/**
 * @version $Revision$ $Date$
 */
public class ExtensiionMapContentTypeResolverTestCase extends TestCase {
    private ContentTypeResolver resolver;

    public void testKnownContentType() throws Exception {
        URL url = getClass().getResource("test.txt");

        assertEquals("text/plain", resolver.getContentType(url));
    }

    public void testGetContentType() throws Exception {
        URL url = getClass().getResource("test.composite");

        assertEquals("text/vnd.fabric3.composite+xml", resolver.getContentType(url));
    }

    protected void setUp() throws Exception {
        super.setUp();
        resolver = new ExtensionMapContentTypeResolver();
    }
}
