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
package org.fabric3.loader.xmlcontribution;

import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.osoa.sca.Constants.SCA_NS;

import java.io.FileNotFoundException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.spi.Namespaces;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.Export;
import org.fabric3.spi.services.contribution.Import;
import org.fabric3.spi.services.contribution.XmlElementManifestProcessor;
import org.fabric3.spi.services.contribution.XmlManifestProcessorRegistry;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class XmlContributionTypeManifestProcessor implements XmlElementManifestProcessor {
    private static final QName XML_CONTRIBUTION = new QName(Namespaces.CORE, "xmlContribution");
    private static final QName SCA_CONTRIBUTION = new QName(SCA_NS, "contribution");
    private XmlManifestProcessorRegistry manifestProcessorRegistry;
    private Loader loader;

    public XmlContributionTypeManifestProcessor(@Reference XmlManifestProcessorRegistry manifestProcessorRegistry, @Reference Loader loader) {
        this.manifestProcessorRegistry = manifestProcessorRegistry;
        this.loader = loader;
    }

    @Init
    public void init() {
        manifestProcessorRegistry.register(this);
    }

    public QName getType() {
        return XML_CONTRIBUTION;
    }

    public void process(ContributionManifest manifest, XMLStreamReader reader, ValidationContext context) throws ContributionException {
        try {
            while (true) {
                int i = reader.next();
                switch (i) {
                case START_ELEMENT:
                    QName qname = reader.getName();
                    if (SCA_CONTRIBUTION.equals(qname)) {
                        ClassLoader cl = getClass().getClassLoader();
                        IntrospectionContext childContext = new DefaultIntrospectionContext(cl, null, null);
                        ContributionManifest embeddedManifest = loader.load(reader, ContributionManifest.class, childContext);
                        if (childContext.hasErrors()) {
                            context.addErrors(childContext.getErrors());
                        }
                        if (childContext.hasWarnings()) {
                            context.addWarnings(childContext.getWarnings());
                        }

                        // merge the contents
                        for (Deployable deployable : embeddedManifest.getDeployables()) {
                            manifest.addDeployable(deployable);
                        }
                        for (Export export : embeddedManifest.getExports()) {
                            manifest.addExport(export);
                        }
                        for (Import imprt : embeddedManifest.getImports()) {
                            manifest.addImport(imprt);
                        }
                    }
                    break;
                case END_ELEMENT:
                    if (SCA_CONTRIBUTION.equals(reader.getName())) {
                        // if we reached here, version was never specified and there are no dependencies
                        return;
                    }
                    break;
                case END_DOCUMENT:
                    return;
                }

            }

        } catch (LoaderException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                return;
            } else {
                throw new ContributionException(e);
            }
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        }

    }


}
