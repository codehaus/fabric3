/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
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
