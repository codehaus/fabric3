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
package org.fabric3.maven;

import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestCase;

/**
 * @author Administrator
 *
 */
public class WarRepositoryHelperTestCase extends TestCase {

    /**
     * @param arg0
     */
    public WarRepositoryHelperTestCase(String arg0) {
        super(arg0);
    }

    /**
     * Test method for {@link org.fabric3.maven.WarRepositoryHelper#WarRepositoryHelper(java.net.URL)}.
     */
    public void testWarRepositoryHelper() {

        URL warUrl = getClass().getClassLoader().getResource("webapp.war");
        URLClassLoader urlc = new URLClassLoader(new URL[] {warUrl});
        
        URL repoUrl = urlc.getResource("WEB-INF/fabric3/");
        System.err.println(repoUrl);
        
        WarRepositoryHelper warRepositoryHelper = new WarRepositoryHelper(repoUrl);
        assertNotNull(warRepositoryHelper);
        
    }

    /**
     * Test method for {@link org.fabric3.maven.WarRepositoryHelper#WarRepositoryHelper(java.net.URL)}.
     */
    public void testResolveTransitively() {

        URL warUrl = getClass().getClassLoader().getResource("webapp.war");
        URLClassLoader urlc = new URLClassLoader(new URL[] {warUrl});
        
        URL repoUrl = urlc.getResource("WEB-INF/fabric3/");
        WarRepositoryHelper warRepositoryHelper = new WarRepositoryHelper(repoUrl);
        
        Artifact artifact = new Artifact();
        artifact.setGroup("commons-httpclient");
        artifact.setName("commons-httpclient");
        artifact.setVersion("3.0");
        
        warRepositoryHelper.resolveTransitively(artifact);
        assertEquals(4, artifact.getUrls().size());
        
    }

}
