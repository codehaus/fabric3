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
