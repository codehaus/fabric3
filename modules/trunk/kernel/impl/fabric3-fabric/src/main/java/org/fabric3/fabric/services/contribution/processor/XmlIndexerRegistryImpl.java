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
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.XmlIndexer;
import org.fabric3.spi.services.contribution.XmlIndexerRegistry;
import org.fabric3.scdl.ValidationContext;

/**
 * Default impelmentation of an XmlIndexerRegistry.
 *
 * @version $Rev$ $Date$
 */
public class XmlIndexerRegistryImpl implements XmlIndexerRegistry {
    private Map<QName, XmlIndexer> cache = new HashMap<QName, XmlIndexer>();

    public void register(XmlIndexer indexer) {
        cache.put(indexer.getType(), indexer);
    }

    public void unregister(QName name) {
        cache.remove(name);
    }

    public void index(Resource resource, XMLStreamReader reader, ValidationContext context) throws ContributionException {
        QName name = reader.getName();
        XmlIndexer indexer = cache.get(name);
        if (indexer == null) {
            return;
        }
        indexer.index(resource, reader, context);
    }
}
