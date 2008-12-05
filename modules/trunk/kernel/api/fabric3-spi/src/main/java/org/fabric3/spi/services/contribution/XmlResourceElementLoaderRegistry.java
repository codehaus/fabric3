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
package org.fabric3.spi.services.contribution;

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.model.type.ValidationContext;

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
     * @throws InstallException if a fatal error loading the resource occurs
     * @throws XMLStreamException    if an error parsing the XML stream occurs
     */
    void load(XMLStreamReader reader, URI contributionUri, Resource resource, ValidationContext context, ClassLoader loader)
            throws InstallException, XMLStreamException;

}