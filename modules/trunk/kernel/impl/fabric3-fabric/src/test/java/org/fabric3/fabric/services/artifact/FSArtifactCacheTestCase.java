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
package org.fabric3.fabric.services.artifact;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.util.io.FileHelper;

/**
 * @version $Revision$ $Date$
 */
public class FSArtifactCacheTestCase extends TestCase {
    private FSArtifactCache cache;

    public void testCache() throws Exception {
        URI uri = URI.create("test");
        InputStream stream = new ByteArrayInputStream("this is a test".getBytes());
        cache.cache(uri, stream);
        URL url = cache.get(uri);
        assertNotNull(url);
        InputStream ret = url.openStream();
        ret.close();
        cache.increment(uri);
        assertNotNull(cache.get(uri));
        cache.release(uri);
        assertNotNull(cache.get(uri));
        cache.release(uri);
        assertNull(cache.get(uri));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getTempDir()).andReturn(new File("tmp_cache"));
        EasyMock.replay(info);
        cache = new FSArtifactCache(info);
        cache.init();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileHelper.deleteDirectory(new File("tmp_cache"));
    }
}
