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
package org.fabric3.introspection.xml;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class LoaderUtilTestCase extends TestCase {
    private NamespaceContext context;
    private String uri;

    public void testQNameWithNoPrefix() {
        assertEquals(new QName(uri, "foo"), LoaderUtil.getQName("foo", uri, null));
    }

    public void testPrefixResolve() {
        EasyMock.expect(context.getNamespaceURI("prefix")).andReturn(uri);
        EasyMock.replay(context);
        QName name = LoaderUtil.getQName("prefix:foo", null, context);
        assertEquals(uri, name.getNamespaceURI());
        assertEquals("prefix", name.getPrefix());
        assertEquals("foo", name.getLocalPart());
        EasyMock.verify(context);
    }


    protected void setUp() throws Exception {
        super.setUp();
        uri = "http://example.com";
        context = EasyMock.createMock(NamespaceContext.class);
    }
}
