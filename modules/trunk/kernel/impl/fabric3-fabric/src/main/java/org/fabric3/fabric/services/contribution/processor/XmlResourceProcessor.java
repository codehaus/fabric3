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
import java.net.URI;
import java.net.URL;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceProcessor;
import org.fabric3.spi.services.contribution.XmlIndexerRegistry;
import org.fabric3.spi.services.contribution.XmlResourceElementLoaderRegistry;
import org.fabric3.services.xmlfactory.XMLFactory;
import org.fabric3.scdl.ValidationContext;

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

    public void index(Contribution contribution, URL url, ValidationContext context) throws ContributionException {
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
            throw new ContributionException(e);
        } catch (IOException e) {
            throw new ContributionException(e);
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

    public void process(URI contributionUri, Resource resource, ValidationContext context, ClassLoader loader) throws ContributionException {
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            stream = resource.getUrl().openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            if (skipToFirstTag(reader)) {
                return;
            }
            elementLoaderRegistry.load(reader, contributionUri, resource, context, loader);
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        } catch (IOException e) {
            throw new ContributionException(e);
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
