/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.spi.contribution.xml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.model.type.ValidationContext;
import org.fabric3.spi.contribution.Resource;

/**
 * A registry of XmlIndexers
 *
 * @version $Rev$ $Date$
 */
public interface XmlIndexerRegistry {
    /**
     * Register a XmlIndexer using the processor's QName type as the key
     *
     * @param indexer the indexer to register
     */
    void register(XmlIndexer indexer);

    /**
     * Unregister an XmlIndexer for a QName
     *
     * @param name the QName
     */
    void unregister(QName name);

    /**
     * Dispatch to an XMLIndexer based on the element type of the resource document tag.
     *
     * @param resource the resource being indexed
     * @param reader   the reader positioned on the start element of the first tag
     * @param context  the context to which validation errors and warnings are reported
     * @throws InstallException if an error occurs during indexing
     */
    void index(Resource resource, XMLStreamReader reader, ValidationContext context) throws InstallException;

}