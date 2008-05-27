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

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.scdl.DefaultValidationContext;

/**
 * @version $Rev$ $Date$
 */
public class MavenPOMProcessorTestCase extends TestCase {
    public static final String NS = "http://maven.apache.org/POM/4.0.0";
    private MavenPOMProcessor processor = new MavenPOMProcessor(null);
    private XMLStreamReader reader;

    public void testParse() throws Exception {
        ContributionManifest manifest = new ContributionManifest();
        ValidationContext context = new DefaultValidationContext();
        processor.process(manifest, reader, context);
        MavenExport export = (MavenExport) manifest.getExports().get(0);
        assertEquals("foo", export.getGroupId());
        assertEquals("bar", export.getArtifactId());
        assertEquals("1.0-SNAPSHOT", export.getVersion());
        assertEquals("jar", export.getClassifier());
        EasyMock.verify(reader);
    }


    protected void setUp() throws Exception {
        super.setUp();
        reader = EasyMock.createMock(XMLStreamReader.class);
        EasyMock.expect(reader.next()).andReturn(START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(new QName(NS, "modelVersion"));
        EasyMock.expect(reader.next()).andReturn(END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(new QName(NS, "modelVersion"));

        EasyMock.expect(reader.next()).andReturn(START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(new QName(NS, "parent"));
        EasyMock.expect(reader.next()).andReturn(END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(new QName(NS, "parent"));

        EasyMock.expect(reader.next()).andReturn(START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(new QName(NS, "groupId"));
        EasyMock.expect(reader.getElementText()).andReturn("foo");
        EasyMock.expect(reader.next()).andReturn(END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(new QName(NS, "groupId"));

        EasyMock.expect(reader.next()).andReturn(START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(new QName(NS, "artifactId"));
        EasyMock.expect(reader.getElementText()).andReturn("bar");
        EasyMock.expect(reader.next()).andReturn(END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(new QName(NS, "artifactId"));

        EasyMock.expect(reader.next()).andReturn(START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(new QName(NS, "packaging"));
        EasyMock.expect(reader.getElementText()).andReturn("jar");
        EasyMock.expect(reader.next()).andReturn(END_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(new QName(NS, "packaging"));

        EasyMock.expect(reader.next()).andReturn(START_ELEMENT);
        EasyMock.expect(reader.getName()).andReturn(new QName(NS, "version"));
        EasyMock.expect(reader.getElementText()).andReturn("1.0-SNAPSHOT");

        EasyMock.replay(reader);
    }


}
