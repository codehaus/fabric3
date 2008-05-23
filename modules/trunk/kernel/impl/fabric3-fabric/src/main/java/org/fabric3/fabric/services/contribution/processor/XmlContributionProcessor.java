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
package org.fabric3.fabric.services.contribution.processor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.services.xmlfactory.XMLFactory;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionProcessor;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.XmlIndexerRegistry;
import org.fabric3.spi.services.contribution.XmlManifestProcessorRegistry;
import org.fabric3.spi.services.contribution.XmlProcessorRegistry;
import org.fabric3.scdl.ValidationContext;

/**
 * Processes an XML-based contribution. The implementaton dispatches to a specific XmlProcessor based on the QName of the document element.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class XmlContributionProcessor implements ContributionProcessor {
    private static final List<String> CONTENT_TYPES = initializeContentTypes();
    private XMLInputFactory xmlFactory;
    private ProcessorRegistry processorRegistry;
    private XmlManifestProcessorRegistry manifestProcessorRegistry;
    private XmlProcessorRegistry xmlProcessorRegistry;
    private XmlIndexerRegistry xmlIndexerRegistry;

    public XmlContributionProcessor(@Reference ProcessorRegistry processorRegistry,
                                    @Reference XmlManifestProcessorRegistry manifestProcessorRegistry,
                                    @Reference XmlProcessorRegistry xmlProcessorRegistry,
                                    @Reference XmlIndexerRegistry xmlIndexerRegistry,
                                    @Reference XMLFactory xmlFactory) {
        this.processorRegistry = processorRegistry;
        this.manifestProcessorRegistry = manifestProcessorRegistry;
        this.xmlProcessorRegistry = xmlProcessorRegistry;
        this.xmlIndexerRegistry = xmlIndexerRegistry;
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
    }

    @Init
    public void init() {
        processorRegistry.register(this);
    }

    public List<String> getContentTypes() {
        return CONTENT_TYPES;
    }

    public void processManifest(Contribution contribution, ValidationContext context) throws ContributionException {
        ContributionManifest manifest = new ContributionManifest();
        contribution.setManifest(manifest);
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            URL locationURL = contribution.getLocation();
            stream = locationURL.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            QName name = reader.getName();
            manifestProcessorRegistry.process(name, manifest, reader, context);
        } catch (IOException e) {
            String uri = contribution.getUri().toString();
            throw new ContributionException("Error processing contribution " + uri, e);
        } catch (XMLStreamException e) {
            String uri = contribution.getUri().toString();
            int line = e.getLocation().getLineNumber();
            int col = e.getLocation().getColumnNumber();
            throw new ContributionException("Error processing contribution " + uri + " [" + line + "," + col + "]", e);
        } finally {
            close(stream, reader);
        }
    }

    public void index(Contribution contribution, ValidationContext context) throws ContributionException {
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
            throw new ContributionException("Error processing contribution " + uri, e);
        } catch (XMLStreamException e) {
            String uri = contribution.getUri().toString();
            int line = e.getLocation().getLineNumber();
            int col = e.getLocation().getColumnNumber();
            throw new ContributionException("Error processing contribution " + uri + " [" + line + "," + col + "]", e);
        } finally {
            close(stream, reader);
        }
    }

    public void process(Contribution contribution, ValidationContext context, ClassLoader loader) throws ContributionException {
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
            throw new ContributionException("Error processing contribution " + uri, e);
        } catch (XMLStreamException e) {
            String uri = contribution.getUri().toString();
            int line = e.getLocation().getLineNumber();
            int col = e.getLocation().getColumnNumber();
            throw new ContributionException("Error processing contribution " + uri + " [" + line + "," + col + "]", e);
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

    private static List<String> initializeContentTypes() {
        List<String> list = new ArrayList<String>(1);
        list.add("application/xml");
        return list;
    }
}
