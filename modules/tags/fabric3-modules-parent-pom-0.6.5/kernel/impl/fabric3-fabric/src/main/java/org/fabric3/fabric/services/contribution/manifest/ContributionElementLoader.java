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
package org.fabric3.fabric.services.contribution.manifest;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderRegistry;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.introspection.xml.UnrecognizedAttribute;
import org.fabric3.introspection.xml.UnrecognizedElement;
import org.fabric3.introspection.xml.UnrecognizedElementException;
import org.fabric3.loader.impl.InvalidQNamePrefix;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;

/**
 * Loads a contribution manifest from a contribution element
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ContributionElementLoader implements TypeLoader<ContributionManifest> {
    private static final QName CONTRIBUTION = new QName(SCA_NS, "contribution");
    private static final QName DEPLOYABLE = new QName(SCA_NS, "deployable");

    private final LoaderRegistry registry;
    private final LoaderHelper helper;

    public ContributionElementLoader(@Reference LoaderRegistry registry,
                                     @Reference LoaderHelper helper) {
        this.registry = registry;
        this.helper = helper;
    }

    @Init
    public void start() {
        registry.registerLoader(CONTRIBUTION, this);
    }

    @Destroy
    public void stop() {
        registry.unregisterLoader(CONTRIBUTION);
    }


    public ContributionManifest load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        ContributionManifest contribution = new ContributionManifest();
        while (true) {
            int event = reader.next();
            switch (event) {
            case START_ELEMENT:
                QName element = reader.getName();
                if (CONTRIBUTION.equals(element)) {
                    continue;
                } else if (DEPLOYABLE.equals(element)) {
                    String name = reader.getAttributeValue(null, "composite");
                    if (name == null) {
                        MissingMainifestAttribute failure =
                                new MissingMainifestAttribute("Composite attribute must be specified", "composite", reader);
                        context.addError(failure);
                        return null;
                    }
                    QName qName;
                    // read qname but only set namespace if it is explicitly declared
                    int index = name.indexOf(':');
                    if (index != -1) {
                        String prefix = name.substring(0, index);
                        String localPart = name.substring(index + 1);
                        String ns = reader.getNamespaceContext().getNamespaceURI(prefix);
                        if (ns == null) {
                            context.addError(new InvalidQNamePrefix(prefix, reader));
                            return null;
                        }
                        qName = new QName(ns, localPart, prefix);
                    } else {
                        qName = new QName(null, name);
                    }
                    Deployable deployable = new Deployable(qName, Constants.COMPOSITE_TYPE);
                    contribution.addDeployable(deployable);
                } else {
                    Object o;
                    try {
                        o = registry.load(reader, Object.class, context);
                    } catch (UnrecognizedElementException e) {
                        UnrecognizedElement failure = new UnrecognizedElement(reader);
                        context.addError(failure);
                        return null;
                    }
                    if (o instanceof Export) {
                        contribution.addExport((Export) o);
                    } else if (o instanceof Import) {
                        contribution.addImport((Import) o);
                    } else if (o != null) {
                        UnrecognizedElement failure = new UnrecognizedElement(reader);
                        context.addError(failure);
                        return null;
                    }
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if (CONTRIBUTION.equals(reader.getName())) {
                    return contribution;
                }
                break;
            }
        }
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"composite".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }


}
