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

package org.fabric3.fabric.services.contribution;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.contribution.ContributionStoreRegistry;

public class ArchiveStoreImplTestCase extends TestCase {
    private ArchiveStoreImpl repository;

    public void testStoreAndFind() throws Exception {
        URI contribution = URI.create("test-resource");
        InputStream contributionStream = new ByteArrayInputStream("test".getBytes());
        repository.store(contribution, contributionStream);
        URL contributionURL = repository.find(contribution);
        assertNotNull(contributionURL);
    }

    public void testList() throws Exception {
        URI contribution = URI.create("test-resource");
        InputStream contributionStream = new ByteArrayInputStream("test".getBytes());
        repository.store(contribution, contributionStream);
        boolean found = false;
        for (URI uri : repository.list()) {
            if (uri.equals(contribution)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    protected void setUp() throws Exception {
        super.setUp();
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getDomain()).andReturn(URI.create("fabric3://./domain/")).anyTimes();
        EasyMock.replay(info);
        ContributionStoreRegistry registry = EasyMock.createNiceMock(ContributionStoreRegistry.class);
        EasyMock.replay(registry);
        this.repository = new ArchiveStoreImpl("target/repository/", registry, info);
        repository.init();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FileHelper.deleteDirectory(new File("target/repository"));
    }
}