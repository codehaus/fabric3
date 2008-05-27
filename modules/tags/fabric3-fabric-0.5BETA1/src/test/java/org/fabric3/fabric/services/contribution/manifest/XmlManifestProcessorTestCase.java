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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.services.xmlfactory.impl.XMLFactoryImpl;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.XmlManifestProcessorRegistry;
import org.fabric3.services.xmlfactory.XMLFactory;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.scdl.DefaultValidationContext;

/**
 * @version $Rev$ $Date$
 */
public class XmlManifestProcessorTestCase extends TestCase {
    public static final QName QNAME = new QName("foo", "bar");
    public static final byte[] XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bar xmlns=\"foo\"/>".getBytes();
    public static final byte[] XML_DTD = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE bar>" +
            "<bar xmlns=\"foo\"/>").getBytes();

    private XmlManifestProcessor processor;
    private XmlManifestProcessorRegistry registry;

    public void testDispatch() throws Exception {
        InputStream stream = new ByteArrayInputStream(XML);
        ContributionManifest manifest = new ContributionManifest();
        ValidationContext context = new DefaultValidationContext();
        processor.process(manifest, stream, context);
        EasyMock.verify(registry);
    }

    public void testDTDDispatch() throws Exception {
        InputStream stream = new ByteArrayInputStream(XML_DTD);
        ContributionManifest manifest = new ContributionManifest();
        ValidationContext context = new DefaultValidationContext();
        processor.process(manifest, stream, context);
        EasyMock.verify(registry);
    }

    protected void setUp() throws Exception {
        super.setUp();
        XMLFactory factory = new XMLFactoryImpl();
        registry = EasyMock.createMock(XmlManifestProcessorRegistry.class);
        registry.process(EasyMock.eq(QNAME),
                         EasyMock.isA(ContributionManifest.class),
                         EasyMock.isA(XMLStreamReader.class),
                         EasyMock.isA(ValidationContext.class));
        EasyMock.replay(registry);
        processor = new XmlManifestProcessor(null, registry, factory);


    }
}
