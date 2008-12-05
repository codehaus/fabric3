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
package org.fabric3.fabric.services.contribution.archive;

import java.net.URL;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.services.contribution.archive.ClasspathProcessorRegistry;

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
        List<URL> urls = processor.process(location);
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
        List<URL> urls = processor.process(location);
        assertEquals(2, urls.size());
        assertEquals(location, urls.get(0));
    }

    protected void setUp() throws Exception {
        super.setUp();
        ClasspathProcessorRegistry registry = EasyMock.createNiceMock(ClasspathProcessorRegistry.class);
        processor = new JarClasspathProcessor(registry);
    }
}
