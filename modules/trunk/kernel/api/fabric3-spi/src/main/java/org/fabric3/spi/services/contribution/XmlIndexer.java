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
 * Implementations index an XML resource corresponding to a given document element QName.
 *
 * @version $Rev$ $Date$
 */
public interface XmlIndexer {

    /**
     * Returns the QName for the XML element type handled by this indexer
     *
     * @return the QName
     */
    QName getType();

    /**
     * Performs the index operation
     *
     * @param resource the resource being indexed
     * @param reader   the reader positioned on the document element
     * @throws ContributionException if an error occurs during indexing
     */
    void index(Resource resource, XMLStreamReader reader) throws ContributionException;

}
