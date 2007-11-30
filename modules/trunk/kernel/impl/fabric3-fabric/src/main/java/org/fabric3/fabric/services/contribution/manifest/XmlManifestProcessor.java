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
package org.fabric3.fabric.services.contribution.manifest;

import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ManifestProcessor;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.XmlManifestProcessorRegistry;
import org.fabric3.spi.services.factories.xml.XMLFactory;

/**
 * Processes XML artifacts in a contribution that contain manifest information. Dispatches to {@link
 * org.fabric3.spi.services.contribution.XmlElementManifestProcessor} based on the document element type for further
 * processing.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class XmlManifestProcessor implements ManifestProcessor {
    private ProcessorRegistry processorRegistry;
    private XmlManifestProcessorRegistry manifestProcessorRegistry;
    private XMLInputFactory xmlFactory;

    public XmlManifestProcessor(@Reference ProcessorRegistry registry,
                                @Reference XmlManifestProcessorRegistry manifestProcessorRegistry,
                                @Reference XMLFactory xmlFactory) {
        this.processorRegistry = registry;
        this.manifestProcessorRegistry = manifestProcessorRegistry;
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
    }

    @Init
    public void init() {
        processorRegistry.register(this);
    }

    public String getContentType() {
        return "application/xml";
    }

    public void process(ContributionManifest manifest, InputStream stream)
            throws ContributionException {
        XMLStreamReader reader = null;
        try {
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            manifestProcessorRegistry.process(reader.getName(), manifest, reader);
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
        }
    }

}
