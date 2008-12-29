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
package org.fabric3.loader.plan;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoaderRegistry;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.plan.DeploymentPlan;

/**
 * @version $Revision$ $Date$
 */
public class DeploymentPlanProcessorTestCase extends TestCase {

    private static final String XML =
            "<?xml version=\"1.0\" encoding=\"ASCII\"?>\n" +
                    "<plan xmlns=\"urn:fabric3.org:core\" name=\"testPlan\">\n" +
                    "   <mappings>\n" +
                    "      <mapping deployable=\"deployable1\" zone=\"zone1\"/>  \n" +
                    "      <mapping deployable=\"deployable2\" zone=\"zone2\"/>  \n" +
                    "   </mappings>\n" +
                    "</plan>";


    private DeploymentPlanProcessor processor;
    private XMLStreamReader reader;

    public void testProcess() throws Exception {
        Resource resource = new Resource(null, "test");
        QName qName = new QName(DeploymentPlanConstants.PLAN_NAMESPACE, "testPlan");
        QNameSymbol symbol = new QNameSymbol(qName);
        ResourceElement<QNameSymbol, DeploymentPlan> element = new ResourceElement<QNameSymbol, DeploymentPlan>(symbol);
        resource.addResourceElement(element);
        URI uri = URI.create("uri");
        IntrospectionContext context = new DefaultIntrospectionContext();
        processor.load(reader, uri, resource, context, getClass().getClassLoader());
        DeploymentPlan plan = element.getValue();
        assertNotNull(plan);
        assertEquals(2, plan.getDeployableMappings().size());
        assertEquals("zone1", plan.getDeployableMappings().get(new QName(null, "deployable1")));
        assertEquals("zone2", plan.getDeployableMappings().get(new QName(null, "deployable2")));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        XmlResourceElementLoaderRegistry registry = EasyMock.createNiceMock(XmlResourceElementLoaderRegistry.class);
        EasyMock.replay(registry);
        processor = new DeploymentPlanProcessor(registry);
        processor.init();
        InputStream stream = new ByteArrayInputStream(XML.getBytes());
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
    }
}