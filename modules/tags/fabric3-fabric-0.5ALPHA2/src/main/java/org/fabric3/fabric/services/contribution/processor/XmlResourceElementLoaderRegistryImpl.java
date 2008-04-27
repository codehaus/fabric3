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
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.XmlResourceElementLoader;
import org.fabric3.spi.services.contribution.XmlResourceElementLoaderRegistry;

/**
 * Default impelmentation of an XmlIndexerRegistry.
 *
 * @version $Rev$ $Date$
 */
public class XmlResourceElementLoaderRegistryImpl implements XmlResourceElementLoaderRegistry {
    private Map<QName, XmlResourceElementLoader> cache = new HashMap<QName, XmlResourceElementLoader>();

    public void register(XmlResourceElementLoader loader) {
        cache.put(loader.getType(), loader);
    }

    public void unregister(QName name) {
        cache.remove(name);
    }

    @SuppressWarnings({"unchecked"})
    public void load(XMLStreamReader reader, URI contributionUri, Resource resource, ClassLoader loader)
            throws ContributionException, XMLStreamException {
        try {
            QName name = reader.getName();
            XmlResourceElementLoader elementLoader = cache.get(name);
            if (elementLoader == null) {
                return;
            }
            elementLoader.load(reader, contributionUri, resource, loader);
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