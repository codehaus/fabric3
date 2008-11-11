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

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.scdl.ValidationContext;

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
     * @param context         the context to which validation errors and warnings are reported
     * @param loader          the classloader to load artifacts in
     * @throws InstallException   if a general load error occurs
     * @throws XMLStreamException if there is an error reading the XML stream
     */
    void load(XMLStreamReader reader, URI contributionUri, Resource resource, ValidationContext context, ClassLoader loader)
            throws InstallException, XMLStreamException;

}
