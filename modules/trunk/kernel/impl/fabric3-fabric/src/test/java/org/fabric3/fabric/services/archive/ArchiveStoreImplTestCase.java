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

package org.fabric3.fabric.services.archive;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.runtime.HostInfo;

public class ArchiveStoreImplTestCase extends TestCase {
    private ArchiveStoreImpl repository;

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
        EasyMock.replay(info);
        this.repository = new ArchiveStoreImpl(info);
        repository.setRepository("target/repository/");
        repository.init();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileHelper.deleteDirectory(new File("target/repository"));
    }
}