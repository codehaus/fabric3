/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.ftp.introspection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.binding.ftp.scdl.FtpBindingDefinition;
import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.xml.LoaderHelper;

/**
 * @version $Revision$ $Date$
 */
public class FtpBindingLoaderTestCase extends TestCase {
    private static final String XML_NO_COMMANDS =
            "<f3:binding.ftp uri=\"ftp://foo.com/service\" xmlns:f3=\"http://fabric3.org/xmlns/sca/2.0-alpha\"></f3:binding.ftp>";

    private static final String XML_COMMANDS =
            "<f3:binding.ftp uri=\"ftp://foo.com/service\" xmlns:f3=\"http://fabric3.org/xmlns/sca/2.0-alpha\">\n" +
                    "   <commands>\n" +
                    "     <command>QUOTE test1</command>\n" +
                    "     <command>QUOTE test2</command>\n" +
                    "   </commands>\n" +
                    "</f3:binding.ftp>";

    private DefaultIntrospectionContext context;
    private FtpBindingLoader loader;

    public void testBindingNoCommandsParse() throws Exception {
        InputStream stream = new ByteArrayInputStream(XML_NO_COMMANDS.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
        FtpBindingDefinition definition = loader.load(reader, context);
        assertNotNull(definition.getTargetUri());
    }

    public void testBindingCommandsParse() throws Exception {
        InputStream stream = new ByteArrayInputStream(XML_COMMANDS.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
        FtpBindingDefinition definition = loader.load(reader, context);
        List<String> commands = definition.getSTORCommands();
        assertEquals(2, commands.size());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = new DefaultIntrospectionContext((URI) null, null, null);

        LoaderHelper helper = EasyMock.createNiceMock(LoaderHelper.class);
        EasyMock.replay(helper);
        loader = new FtpBindingLoader(helper);

    }
}
