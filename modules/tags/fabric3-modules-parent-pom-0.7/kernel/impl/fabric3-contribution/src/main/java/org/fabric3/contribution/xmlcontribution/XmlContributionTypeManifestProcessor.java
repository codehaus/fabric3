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

import java.io.FileNotFoundException;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.model.type.ValidationContext;
import org.fabric3.spi.Namespaces;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.manifest.XmlElementManifestProcessor;
import org.fabric3.spi.contribution.manifest.XmlManifestProcessorRegistry;

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

    public void process(ContributionManifest manifest, XMLStreamReader reader, ValidationContext context) throws InstallException {
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
                throw new InstallException(e);
            }
        } catch (XMLStreamException e) {
            throw new InstallException(e);
        }

    }


}
