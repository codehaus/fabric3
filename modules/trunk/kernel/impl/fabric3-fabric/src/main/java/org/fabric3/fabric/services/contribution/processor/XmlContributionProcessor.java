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
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionProcessor;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.XmlProcessor;
import org.fabric3.spi.services.contribution.XmlProcessorRegistry;

/**
 * Processes an XML-based contribution. The implementaton dispatches to a specific XmlProcessor based on the QName of
 * the document element.
 *
 * @version $Rev$ $Date$
 */
@Service(XmlProcessorRegistry.class)
@EagerInit
public class XmlContributionProcessor implements ContributionProcessor, XmlProcessorRegistry {
    private XMLInputFactory xmlFactory;
    private Map<QName, XmlProcessor> processors = new HashMap<QName, XmlProcessor>();

    public XmlContributionProcessor(@Reference ProcessorRegistry processorRegistry,
                                    @Reference XMLInputFactory xmlFactory) {
        this.xmlFactory = xmlFactory;
        processorRegistry.register(this);
    }

    public String[] getContentTypes() {
        return new String[]{"application/xml"};
    }

    public void processManifest(Contribution contribution) throws ContributionException {
        // TODO support XSD import?
    }

    public void register(XmlProcessor processor) {
        processors.put(processor.getType(), processor);
    }

    public void unregisterContributionProcessor(QName name) {
        processors.remove(name);
    }

    public void processContent(Contribution contribution, URI source) throws ContributionException {
        processContent(contribution);
    }

    public void processContent(Contribution contribution) throws ContributionException {
        URL locationURL = contribution.getLocation();
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            stream = locationURL.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            QName name = reader.getName();
            XmlProcessor processor = processors.get(name);
            if (processor == null) {
                throw new XmlProcessorTypeNotFoundException("XML processor not found for", name.toString());
            }
            processor.processContent(contribution, reader);
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
                // TODO log exception
                e.printStackTrace();
            }
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                // TODO log exception
                e.printStackTrace();
            }
        }
    }

}
