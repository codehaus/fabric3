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

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.XmlProcessor;
import org.fabric3.spi.services.contribution.XmlProcessorRegistry;
import org.fabric3.spi.services.contribution.XmlResourceElementLoader;

/**
 * Processes a contributed definitions file.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class DefinitionsProcessor implements XmlProcessor {
    private static final QName DEFINITIONS = new QName(SCA_NS, "definitions");
    private XmlResourceElementLoader loader;

    public DefinitionsProcessor(@Reference(name = "processorRegistry")XmlProcessorRegistry processorRegistry,
                                @Reference(name = "loader")XmlResourceElementLoader loader) {
        this.loader = loader;
        processorRegistry.register(this);
    }

    public QName getType() {
        return DEFINITIONS;
    }

    public void processContent(Contribution contribution, XMLStreamReader reader) throws ContributionException {
        try {
            ClassLoader cl = getClass().getClassLoader();
            URI uri = contribution.getUri();
            assert contribution.getResources().size() == 1;
            Resource resource = contribution.getResources().get(0);
            loader.load(reader, uri, resource, cl);
        } catch (XMLStreamException e) {
            throw new ContributionException(e);
        }
    }
}
