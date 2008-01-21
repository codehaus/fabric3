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
package org.fabric3.spi.services.contribution;

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.host.contribution.ContributionException;

/**
 * A registry of XmlResourceElementLoaders
 *
 * @version $Rev$ $Date$
 */
public interface XmlResourceElementLoaderRegistry {
    /**
     * Register a XmlResourceElementLoader using the processor's QName type as the key
     *
     * @param indexer the indexer to register
     */
    void register(XmlResourceElementLoader indexer);

    /**
     * Unregister an XmlResourceElementLoader for a QName
     *
     * @param name the QName
     */
    void unregister(QName name);

    void load(XMLStreamReader reader, URI contributionUri, Resource resource, ClassLoader loader)
            throws ContributionException, XMLStreamException;

}