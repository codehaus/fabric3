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
package org.fabric3.fabric.services.contribution;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.services.archive.ArchiveStore;
import org.fabric3.spi.services.contribution.ArtifactResolverRegistry;


/**
 * @version $Rev$ $Date$
 */
public class HttpResolverTestCase extends TestCase {
    private HttpResolver resolver;
    private ArchiveStore store;
    private URL url;
    private File file;

    public void testResolve() throws Exception {
        EasyMock.expect(store.find(url.toURI())).andReturn(null);
        EasyMock.expect(store.store(EasyMock.isA(URI.class), EasyMock.isA(InputStream.class))).andReturn(url);
        EasyMock.replay(store);
        resolver.resolve(url);
        EasyMock.verify(store);
    }


    protected void setUp() throws Exception {
        super.setUp();
        ArtifactResolverRegistry registry = EasyMock.createMock(ArtifactResolverRegistry.class);
        registry.register(EasyMock.eq("http"), EasyMock.isA(HttpResolver.class));
        EasyMock.replay(registry);
        store = EasyMock.createMock(ArchiveStore.class);
        resolver = new HttpResolver(registry, store);
        file = new File("test.txt");
        FileOutputStream stream = new FileOutputStream(file);
        stream.write("test".getBytes());
        stream.close();
        url = file.toURL();
    }


    protected void tearDown() throws Exception {
        super.tearDown();
        file.delete();
    }
}
