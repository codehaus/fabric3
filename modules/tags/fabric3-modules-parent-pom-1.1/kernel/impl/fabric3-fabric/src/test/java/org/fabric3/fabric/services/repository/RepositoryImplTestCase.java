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
package org.fabric3.fabric.services.repository;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.util.io.FileHelper;

public class RepositoryImplTestCase extends TestCase {
    private RepositoryImpl repository;

    public void testStoreAndFind() throws Exception {
        URI uri = URI.create("test-resource");
        InputStream archiveStream = new ByteArrayInputStream("test".getBytes());
        repository.store(uri, archiveStream);
        URL contributionURL = repository.find(uri);
        assertNotNull(contributionURL);
    }

    public void testList() throws Exception {
        URI archiveUri = URI.create("test-resource");
        InputStream archiveStream = new ByteArrayInputStream("test".getBytes());
        repository.store(archiveUri, archiveStream);
        boolean found = false;
        for (URI uri : repository.list()) {
            if (uri.equals(archiveUri)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    protected void setUp() throws Exception {
        super.setUp();
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getBaseDir()).andReturn(null).atLeastOnce();
        EasyMock.expect(info.getTempDir()).andReturn(null).atLeastOnce();
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.VM).atLeastOnce();
        EasyMock.replay(info);
        File repository = new File("repository");
        FileHelper.forceMkdir(new File(repository, "cache"));
        this.repository = new RepositoryImpl(info);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileHelper.forceDelete(new File("repository"));
    }
}