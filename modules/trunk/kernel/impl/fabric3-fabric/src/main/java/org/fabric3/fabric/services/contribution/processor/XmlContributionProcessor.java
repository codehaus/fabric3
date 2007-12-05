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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionProcessor;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.XmlIndexerRegistry;
import org.fabric3.spi.services.contribution.XmlProcessorRegistry;
import org.fabric3.spi.services.factories.xml.XMLFactory;

/**
 * Processes an XML-based contribution. The implementaton dispatches to a specific XmlProcessor based on the QName of
 * the document element.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class XmlContributionProcessor implements ContributionProcessor {
    private XMLInputFactory xmlFactory;
    private ProcessorRegistry processorRegistry;
    private XmlProcessorRegistry xmlProcessorRegistry;
    private XmlIndexerRegistry xmlIndexerRegistry;

    public XmlContributionProcessor(@Reference ProcessorRegistry processorRegistry,
                                    @Reference XmlProcessorRegistry xmlProcessorRegistry,
                                    @Reference XmlIndexerRegistry xmlIndexerRegistry,
                                    @Reference XMLFactory xmlFactory) {
        this.processorRegistry = processorRegistry;
        this.xmlProcessorRegistry = xmlProcessorRegistry;
        this.xmlIndexerRegistry = xmlIndexerRegistry;
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
    }

    @Init
    public void init() {
        processorRegistry.register(this);
    }

    public String[] getContentTypes() {
        return new String[]{"application/xml"};
    }

    public void processManifest(Contribution contribution) throws ContributionException {
        ContributionManifest manifest = new ContributionManifest();
        contribution.setManifest(manifest);
    }

    public void index(Contribution contribution) throws ContributionException {
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            URL locationURL = contribution.getLocation();
            stream = locationURL.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            Resource resource = new Resource(contribution.getLocation(), "application/xml");
            xmlIndexerRegistry.index(resource, reader);
            contribution.addResource(resource);
        } catch (IOException e) {
            throw new ContributionException(e);
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        } finally {
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
    }

    public void processContent(Contribution contribution, ClassLoader loader) throws ContributionException {
        URL locationURL = contribution.getLocation();
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            stream = locationURL.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            xmlProcessorRegistry.process(contribution, reader);
        } catch (IOException e) {
            throw new ContributionException(e);
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        } finally {
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
    }

}
