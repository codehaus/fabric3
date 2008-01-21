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

import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.XmlElementManifestProcessor;
import org.fabric3.spi.services.contribution.XmlManifestProcessorRegistry;

/**
 * Default implementation of XmlManifestProcessorRegistry.
 *
 * @version $Rev$ $Date$
 */
public class XmlManifestProcessorRegistryImpl implements XmlManifestProcessorRegistry {

    private Map<QName, XmlElementManifestProcessor> cache = new HashMap<QName, XmlElementManifestProcessor>();

    public void register(XmlElementManifestProcessor processor) {
        cache.put(processor.getType(), processor);
    }

    public void unregisterProcessor(QName name) {
        cache.remove(name);
    }

    public void process(QName name, ContributionManifest manifest, XMLStreamReader reader)
            throws ContributionException {
        XmlElementManifestProcessor processor = cache.get(name);
        if (processor == null) {
            return;
        }
        processor.process(manifest, reader);
    }
}
