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
package org.fabric3.loader.xmlcontribution;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.ValidationContext;
import static org.fabric3.spi.Constants.FABRIC3_NS;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.contribution.XmlIndexer;
import org.fabric3.spi.services.contribution.XmlIndexerRegistry;
import org.fabric3.introspection.xml.MissingAttribute;

/**
 * Indexer for the <xmlContribution> type.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class XmlContributionTypeIndexer implements XmlIndexer {
    private static final QName XML_CONTRIBUTION = new QName(FABRIC3_NS, "xmlContribution");
    private static final QName COMPOSITE = new QName(SCA_NS, "composite");
    private XmlIndexerRegistry registry;


    public XmlContributionTypeIndexer(@Reference XmlIndexerRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public QName getType() {
        return XML_CONTRIBUTION;
    }

    public void index(Resource resource, XMLStreamReader reader, ValidationContext context) throws ContributionException {

        while (true) {
            try {
                switch (reader.next()) {
                case START_ELEMENT:
                    QName qname = reader.getName();
                    if (COMPOSITE.equals(qname)) {
                        String name = reader.getAttributeValue(null, "name");
                        if (name == null) {
                            context.addError(new MissingAttribute("Composite name not specified", "name", reader));
                            return;
                        }
                        String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
                        QName compositeName = new QName(targetNamespace, name);
                        QNameSymbol symbol = new QNameSymbol(compositeName);
                        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol);
                        resource.addResourceElement(element);
                        break;
                    } else {
                        // unknown element, just skip
                        continue;
                    }
                case XMLStreamConstants.END_DOCUMENT:
                    return;
                }
            } catch (XMLStreamException e) {
                throw new ContributionException("Error processing resource: " + resource.getUrl().toString(), e);
            }
        }

    }

}