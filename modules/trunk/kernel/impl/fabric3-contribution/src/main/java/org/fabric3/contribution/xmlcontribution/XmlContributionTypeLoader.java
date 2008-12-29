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
package org.fabric3.contribution.xmlcontribution;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.contribution.Constants;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.ValidationContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElementException;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.Namespaces;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceElementNotFoundException;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.contribution.xml.XmlProcessor;
import org.fabric3.spi.contribution.xml.XmlProcessorRegistry;

/**
 * Loader for definitions.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class XmlContributionTypeLoader implements XmlProcessor {
    private static final QName XML_CONTRIBUTION = new QName(Namespaces.CORE, "xmlContribution");
    static final QName COMPOSITE = new QName(SCA_NS, "composite");

    private XmlProcessorRegistry processorRegistry;
    private Loader loader;

    public XmlContributionTypeLoader(@Reference XmlProcessorRegistry processorRegistry, @Reference Loader loader) {
        this.processorRegistry = processorRegistry;
        this.loader = loader;
    }

    @Init
    public void init() {
        processorRegistry.register(this);
    }

    public QName getType() {
        return XML_CONTRIBUTION;
    }

    public void processContent(Contribution contribution, ValidationContext context, XMLStreamReader reader, ClassLoader classLoader)
            throws InstallException {
        validateAttributes(reader, context);
        List<Composite> composites = new ArrayList<Composite>();
        String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
        URI contributionUri = contribution.getUri();
        try {
            IntrospectionContext childContext = new DefaultIntrospectionContext(contributionUri, classLoader, targetNamespace);
            while (true) {
                switch (reader.next()) {
                case START_ELEMENT:
                    QName qname = reader.getName();
                    Composite definition = null;
                    if (COMPOSITE.equals(qname)) {
                        try {
                            definition = loader.load(reader, Composite.class, childContext);
                        } catch (UnrecognizedElementException e) {
                            throw new InstallException("Error processing contribution: " + contributionUri.toString(), e);
                        }
                    }
                    if (definition != null) {
                        composites.add(definition);
                    }
                    break;
                case END_ELEMENT:
                    QName name = reader.getName();
                    if (XML_CONTRIBUTION.equals(name)) {
                        for (Composite composite : composites) {
                            boolean found = false;
                            Symbol candidateSymbol = new QNameSymbol(composite.getName());
                            for (Resource resource : contribution.getResources()) {
                                for (ResourceElement element : resource.getResourceElements()) {
                                    if (element.getSymbol().equals(candidateSymbol)) {
                                        element.setValue(composite);
                                        found = true;
                                        break;
                                    }
                                }
                                if (found) {
                                    break;
                                }
                            }
                            if (!found) {
                                String id = composite.getName().toString();
                                // xcv should this be thrown?
                                throw new ResourceElementNotFoundException("Composite not found: " + id, id);
                            }
                        }
                        ContributionManifest manifest = contribution.getManifest();
                        // if no deployables are specified, assume all composites are
                        if (manifest.getDeployables().isEmpty()) {
                            for (Composite composite : composites) {
                                Deployable deployable = new Deployable(composite.getName(), Constants.COMPOSITE_TYPE);
                                manifest.addDeployable(deployable);
                            }
                        }
                        if (childContext.hasErrors()) {
                            context.addErrors(childContext.getErrors());
                        }
                        if (childContext.hasWarnings()) {
                            context.addWarnings(childContext.getWarnings());
                        }
                        return;
                    }
                    // update indexed elements with the loaded definitions
                }
            }
        } catch (XMLStreamException e) {
            String uri = contribution.getUri().toString();
            throw new InstallException("Error processing contribution: " + uri, e);
        }

    }

    private void validateAttributes(XMLStreamReader reader, ValidationContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"targetNamespace".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }


}
