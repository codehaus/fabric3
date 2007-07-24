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
package org.fabric3.fabric.services.contribution.processor;

import java.io.File;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.services.archive.JarServiceImpl;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;

/**
 * @version $Rev$ $Date$
 */
public class JarClasspathProcessorTestCase extends TestCase {
    private JarClasspathProcessor processor;

    /**
     * Verifies processing when no jars are present in META-INF/lib
     *
     * @throws Exception
     */
    public void testExpansionNoLibraries() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL location = cl.getResource("./repository/1/test.jar");
        List<URL> urls = processor.process(new File(location.getFile()));
        assertEquals(1, urls.size());
        assertEquals(location, urls.get(0));
    }

    /**
     * Verifies jars in META-INF/lib are added to the classpath
     *
     * @throws Exception
     */
    public void testExpansionWithLibraries() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL location = cl.getResource("./repository/2/testWithLibraries.jar");
        List<URL> urls = processor.process(new File(location.getFile()));
        assertEquals(2, urls.size());
        assertEquals(location, urls.get(0));
        String url = urls.get(1).toString();
        assertTrue(url.endsWith("/META-INF/lib/test.jar"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        ClasspathProcessorRegistry registry = EasyMock.createNiceMock(ClasspathProcessorRegistry.class);
        processor = new JarClasspathProcessor(registry, new JarServiceImpl());
    }
}
