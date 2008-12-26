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
package org.fabric3.contribution.processor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
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
import org.fabric3.model.type.ValidationContext;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.xml.XmlIndexerRegistry;
import org.fabric3.spi.contribution.xml.XmlProcessorRegistry;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Processes an XML-based contribution. The implementaton dispatches to a specific XmlProcessor based on the QName of the document element.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class XmlContributionProcessor implements ContributionProcessor {
    private static final List<String> CONTENT_TYPES = initializeContentTypes();
    private static final QName SCA_CONTRIBUTION = new QName(SCA_NS, "contribution");
    private XMLInputFactory xmlFactory;
    private ProcessorRegistry processorRegistry;
    private XmlProcessorRegistry xmlProcessorRegistry;
    private XmlIndexerRegistry xmlIndexerRegistry;
    private Loader loader;

    public XmlContributionProcessor(@Reference ProcessorRegistry processorRegistry,
                                    @Reference XmlProcessorRegistry xmlProcessorRegistry,
                                    @Reference XmlIndexerRegistry xmlIndexerRegistry,
                                    @Reference Loader loader,
                                    @Reference XMLFactory xmlFactory) {
        this.processorRegistry = processorRegistry;
        this.xmlProcessorRegistry = xmlProcessorRegistry;
        this.xmlIndexerRegistry = xmlIndexerRegistry;
        this.loader = loader;
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
    }

    @Init
    public void init() {
        processorRegistry.register(this);
    }

    public List<String> getContentTypes() {
        return CONTENT_TYPES;
    }

    public void processManifest(Contribution contribution, ValidationContext context) throws InstallException {
        ContributionManifest manifest = contribution.getManifest();
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            URL locationURL = contribution.getLocation();
            stream = locationURL.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            processManifest(manifest, reader, context);
        } catch (IOException e) {
            String uri = contribution.getUri().toString();
            throw new InstallException("Error processing contribution " + uri, e);
        } catch (XMLStreamException e) {
            String uri = contribution.getUri().toString();
            int line = e.getLocation().getLineNumber();
            int col = e.getLocation().getColumnNumber();
            throw new InstallException("Error processing contribution " + uri + " [" + line + "," + col + "]", e);
        } finally {
            close(stream, reader);
        }
    }

    public void index(Contribution contribution, ValidationContext context) throws InstallException {
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            URL locationURL = contribution.getLocation();
            stream = locationURL.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            Resource resource = new Resource(contribution.getLocation(), "application/xml");
            xmlIndexerRegistry.index(resource, reader, context);
            contribution.addResource(resource);
        } catch (IOException e) {
            String uri = contribution.getUri().toString();
            throw new InstallException("Error processing contribution " + uri, e);
        } catch (XMLStreamException e) {
            String uri = contribution.getUri().toString();
            int line = e.getLocation().getLineNumber();
            int col = e.getLocation().getColumnNumber();
            throw new InstallException("Error processing contribution " + uri + " [" + line + "," + col + "]", e);
        } finally {
            close(stream, reader);
        }
    }

    public void process(Contribution contribution, ValidationContext context, ClassLoader loader) throws InstallException {
        URL locationURL = contribution.getLocation();
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            stream = locationURL.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            xmlProcessorRegistry.process(contribution, reader, context, loader);
        } catch (IOException e) {
            String uri = contribution.getUri().toString();
            throw new InstallException("Error processing contribution " + uri, e);
        } catch (XMLStreamException e) {
            String uri = contribution.getUri().toString();
            int line = e.getLocation().getLineNumber();
            int col = e.getLocation().getColumnNumber();
            throw new InstallException("Error processing contribution " + uri + " [" + line + "," + col + "]", e);
        } finally {
            close(stream, reader);
        }
    }

    private void close(InputStream stream, XMLStreamReader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processManifest(ContributionManifest manifest, XMLStreamReader reader, ValidationContext context) throws InstallException {
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

    private static List<String> initializeContentTypes() {
        List<String> list = new ArrayList<String>(1);
        list.add("application/xml");
        return list;
    }
}
