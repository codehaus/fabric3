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
import org.fabric3.scdl.ValidationContext;

/**
 * Processes an XML-based contribution
 *
 * @version $Rev$ $Date$
 */
public interface XmlProcessor {

    /**
     * Returns the QName for the type of XML contribution handled by this processor
     *
     * @return the QName
     */
    QName getType();

    /**
     * Processes the XML contribution
     *
     * @param contribution the contribution metadata to update
     * @param context      the context to which validation errors and warnings are reported
     * @param reader       the reader positioned at the first element of the document
     * @param loader       the classloader to perform resolution in
     * @throws ContributionException if an error occurs processing
     */
    void processContent(Contribution contribution, ValidationContext context, XMLStreamReader reader, ClassLoader loader)
            throws ContributionException;
}
