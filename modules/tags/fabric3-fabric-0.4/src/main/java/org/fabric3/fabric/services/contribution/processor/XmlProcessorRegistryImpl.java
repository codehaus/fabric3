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

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.XmlProcessor;
import org.fabric3.spi.services.contribution.XmlProcessorRegistry;

/**
 * Default impelmentation of an XmlProcessorRegistry.
 *
 * @version $Rev$ $Date$
 */
public class XmlProcessorRegistryImpl implements XmlProcessorRegistry {
    private Map<QName, XmlProcessor> cache = new HashMap<QName, XmlProcessor>();

    public void register(XmlProcessor processor) {
        cache.put(processor.getType(), processor);
    }

    public void unregister(QName name) {
        cache.remove(name);
    }

    public void process(Contribution contribution, XMLStreamReader reader) throws ContributionException {
        QName name = reader.getName();
        XmlProcessor processor = cache.get(name);
        if (processor == null) {
            throw new XmlProcessorTypeNotFoundException("XML processor not found for", name.toString());
        }
        processor.processContent(contribution, reader);
    }
}