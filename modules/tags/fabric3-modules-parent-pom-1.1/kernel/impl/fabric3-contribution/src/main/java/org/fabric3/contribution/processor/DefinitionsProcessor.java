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
package org.fabric3.contribution.processor;

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.oasisopen.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.xml.XmlProcessor;
import org.fabric3.spi.contribution.xml.XmlProcessorRegistry;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoader;

/**
 * Processes a contributed definitions file.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class DefinitionsProcessor implements XmlProcessor {
    private static final QName DEFINITIONS = new QName(SCA_NS, "definitions");
    private XmlResourceElementLoader loader;

    public DefinitionsProcessor(@Reference(name = "processorRegistry")XmlProcessorRegistry processorRegistry,
                                @Reference(name = "loader")XmlResourceElementLoader loader) {
        this.loader = loader;
        processorRegistry.register(this);
    }

    public QName getType() {
        return DEFINITIONS;
    }

    public void processContent(Contribution contribution, IntrospectionContext context, XMLStreamReader reader, ClassLoader cl)
            throws InstallException {
        try {
            URI uri = contribution.getUri();
            assert contribution.getResources().size() == 1;
            Resource resource = contribution.getResources().get(0);
            loader.load(reader, uri, resource, context, cl);
        } catch (XMLStreamException e) {
            throw new InstallException(e);
        }
    }
}
