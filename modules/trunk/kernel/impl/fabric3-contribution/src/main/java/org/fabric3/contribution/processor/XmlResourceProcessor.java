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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.xml.XMLFactory;
import org.fabric3.spi.introspection.ValidationContext;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.contribution.xml.XmlIndexerRegistry;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoaderRegistry;

/**
 * Processes an XML-based resource in a contribution, delegating to a an XMLIndexer to index the resource and a Loader to load it based on the root
 * element QName.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class XmlResourceProcessor implements ResourceProcessor {
    private ProcessorRegistry processorRegistry;
    private XmlResourceElementLoaderRegistry elementLoaderRegistry;
    private XmlIndexerRegistry indexerRegistry;
    private XMLInputFactory xmlFactory;

    public XmlResourceProcessor(@Reference ProcessorRegistry processorRegistry,
                                @Reference XmlIndexerRegistry indexerRegistry,
                                @Reference XmlResourceElementLoaderRegistry elementLoaderRegistry,
                                @Reference XMLFactory xmlFactory) {
        this.processorRegistry = processorRegistry;
        this.elementLoaderRegistry = elementLoaderRegistry;
        this.indexerRegistry = indexerRegistry;
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
    }

    @Init
    public void init() {
        processorRegistry.register(this);
    }

    public String getContentType() {
        return "application/xml";
    }

    public void index(Contribution contribution, URL url, ValidationContext context) throws InstallException {
        XMLStreamReader reader = null;
        InputStream stream = null;
        try {
            stream = url.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            if (skipToFirstTag(reader)) {
                return;
            }
            Resource resource = new Resource(url, "application/xml");
            indexerRegistry.index(resource, reader, context);
            contribution.addResource(resource);
        } catch (XMLStreamException e) {
            throw new InstallException(e);
        } catch (IOException e) {
            throw new InstallException(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }

    public void process(URI contributionUri, Resource resource, ValidationContext context, ClassLoader loader) throws InstallException {
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            stream = resource.getUrl().openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            if (skipToFirstTag(reader)) {
                resource.setProcessed(true);
                return;
            }
            elementLoaderRegistry.load(reader, contributionUri, resource, context, loader);
            resource.setProcessed(true);
        } catch (XMLStreamException e) {
            throw new InstallException(e);
        } catch (IOException e) {
            throw new InstallException(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean skipToFirstTag(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext() && XMLStreamConstants.START_ELEMENT != reader.getEventType()) {
            reader.next();
        }
        return XMLStreamConstants.END_DOCUMENT == reader.getEventType();
    }

}
