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
import org.fabric3.scdl.ValidationContext;

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

    /**
     * Dispatches to an XmlResourceElementLoader to loads an element in an XML resource
     *
     * @param reader          the StAX reader, positioned at the start of the element to laod
     * @param contributionUri the current contribution URI
     * @param resource        the resource
     * @param context         the context to which validation errors and warnings are reported
     * @param loader          the classloader to resolve resources with
     * @throws ContributionException if a fatal error loading the resource occurs
     * @throws XMLStreamException    if an error parsing the XML stream occurs
     */
    void load(XMLStreamReader reader, URI contributionUri, Resource resource, ValidationContext context, ClassLoader loader)
            throws ContributionException, XMLStreamException;

}