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
 * A registry of XmlProcessors
 *
 * @version $Rev$ $Date$
 */
public interface XmlProcessorRegistry {
    /**
     * Register a XmlProcessor using the processor's QName type as the key
     *
     * @param processor the processor to register
     */
    void register(XmlProcessor processor);

    /**
     * Unregister an XmlProcessor for a QName
     *
     * @param name the QName
     */
    void unregister(QName name);

    /**
     * Dispatches to an XmlProcessor
     *
     * @param contribution the contribution metadata to update
     * @param reader       the reader positioned at the first element of the document
     * @param loader       the classloader to perform resolution in
     * @throws ContributionException if an error occurs processing
     */
    void process(Contribution contribution, XMLStreamReader reader, ClassLoader loader) throws ContributionException;

}
