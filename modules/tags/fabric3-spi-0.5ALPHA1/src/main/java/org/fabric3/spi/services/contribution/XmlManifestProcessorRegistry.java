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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.host.contribution.ContributionException;

/**
 * A registry of XmlElementManifestProcessors.
 *
 * @version $Rev$ $Date$
 */
public interface XmlManifestProcessorRegistry {
    /**
     * Register a XmlElementManifestProcessor using the processor's QName type as the key
     *
     * @param processor the processor to register
     */
    void register(XmlElementManifestProcessor processor);

    /**
     * Unregister an XmlElementManifestProcessor for a QName
     *
     * @param name the QName
     */
    void unregisterProcessor(QName name);

    /**
     * Dispatches to an XmlElementManifestProcessor based on the given Qname.
     *
     * @param name     the document element type to dispatch on
     * @param manifest the manifest being processed
     * @param reader   the reader position on the document element start tag
     * @throws ContributionException if an error occurs during processing
     */
    void process(QName name, ContributionManifest manifest, XMLStreamReader reader) throws ContributionException;

}