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
package org.fabric3.fabric.services.contribution.manifest;

import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.services.contribution.MavenImport;

/**
 * @version $Rev$ $Date$
 */
public class MavenImportLoaderTestCase extends TestCase {
    private MavenImportLoader loader = new MavenImportLoader();
    private XMLStreamReader reader;

    public void testRead() throws Exception {
        MavenImport imprt = loader.load(reader, null);
        assertEquals("foo", imprt.getGroupId());
        assertEquals("bar", imprt.getArtifactId());
        assertEquals("1.0-SNAPSHOT", imprt.getVersion());
        assertEquals("zip", imprt.getClassifier());
    }


    protected void setUp() throws Exception {
        super.setUp();
        reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.getAttributeCount()).andReturn(0);
        EasyMock.expect(reader.getAttributeValue(null, "groupId")).andReturn("foo");
        EasyMock.expect(reader.getAttributeValue(null, "artifactId")).andReturn("bar");
        EasyMock.expect(reader.getAttributeValue(null, "version")).andReturn("1.0-SNAPSHOT");
        EasyMock.expect(reader.getAttributeValue(null, "classifier")).andReturn("zip");
        EasyMock.replay(reader);
    }
}
