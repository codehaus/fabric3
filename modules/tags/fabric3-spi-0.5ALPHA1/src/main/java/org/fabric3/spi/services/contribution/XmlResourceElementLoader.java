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
 * Loads the value of a ResourceElement from an XML artifact.
 *
 * @version $Rev$ $Date$
 */
public interface XmlResourceElementLoader {

    /**
     * Returns the QName of the element type this loader handles.
     *
     * @return the QName of the element type this loader handles
     */
    QName getType();

    /**
     * Loads the element.
     *
     * @param reader          the reader positioned on the first element
     * @param contributionUri the current contribution URI
     * @param resource        the resource that contains the element
     * @param loader          the classloader to load artifacts in
     * @throws ContributionException if a general load error occurs
     * @throws XMLStreamException    if there is an error reading the XML stream
     */
    void load(XMLStreamReader reader, URI contributionUri, Resource resource, ClassLoader loader)
            throws ContributionException, XMLStreamException;

}
